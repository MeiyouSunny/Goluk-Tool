--nethttpheaders接口封装
--

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/tapi"
require"lua/moduledata"
require"lua/location"
require"lua/settingconfig"

local _gHeaders = nil
local _gValues = nil
local _gCount = 0
local _gLocationLon = 0
local _gLocationLat = 0
local _gLocationSpeed = 0
local _gLocationCourse = 0
local _gLocationAltitude = 0
local _gLocationRadius = 0
local _gUid = 0
local _gGender = 0

local function GetMyLocation()
	local nlon,nlat,speed,course,altitude,radius = tiros.location.lkgetlastposition_mem()
	if nlon == nil or nlat == nil then
		nlon,nlat = tiros.location.lkgetlastposition_file()
		if nlon ~= nil or nlat ~= nil then
			nlon = tostring(math.ceil(nlon))
			nlat = tostring(math.ceil(nlat))
			radius = 0;
		else
			nlon = "419031106"
			nlat = "143670827"
			radius = 0;
		end
	else
		if nlon == 0 or nlat == 0 then
			nlon = "419031106"
			nlat = "143670827"
			speed = "0";
			course = "0";
			altitude = "0";
			radius = "0";
		end
		nlon = tostring(math.ceil(nlon))
		nlat = tostring(math.ceil(nlat))
		speed = tostring(speed)
		course = tostring(course)
		altitude = tostring(altitude)
		radius = tostring(radius)
	end
	return nlon,nlat,speed,course,altitude,radius
end

local function httpinit()
	if _gHeaders == nil or _gValues == nil then
		_gHeaders = {};
		_gValues = {};
		_gCount = 0;
		local mobileid = tiros.moduledata.moduledata_get('framework','mobileid');
		if mobileid == nil then
			mobileid = tiros.tapi.tapigetmobileid();
		end
		if mobileid ~= nil then
			_gHeaders[_gCount+1] = "mobileid"
			_gValues[_gCount+1] = mobileid
			_gCount = _gCount + 1
		end
		local version = tiros.moduledata.moduledata_get('framework','version');
		if version == nil then
			version = "0.0.0";
		end
		if version ~= nil then
			_gHeaders[_gCount+1] = "version"
			_gValues[_gCount+1] = version
			_gCount = _gCount + 1
		end
		local uid = tiros.moduledata.moduledata_get('framework','uid');
		if uid == nil then
			uid = "";
		end
		--if uid ~= nil then
			_gHeaders[_gCount+1] = "uid"
			_gValues[_gCount+1] = uid
			_gCount = _gCount + 1
			_gUid = _gCount
		--end
		local nlon,nlat,speed,course,altitude,radius = GetMyLocation()
		if nlon ~= nil then
			_gHeaders[_gCount + 1] = "selflon"
			_gValues[_gCount + 1] = nlon
			_gCount = _gCount + 1
			_gLocationLon = _gCount
		end
		if nlat ~= nil then
			_gHeaders[_gCount + 1] = "selflat"
			_gValues[_gCount + 1] = nlat
			_gCount = _gCount + 1
			_gLocationLat = _gCount
		end
		if speed ~= nil then
			_gHeaders[_gCount + 1] = "speed"
			_gValues[_gCount + 1] = speed
			_gCount = _gCount + 1
			_gLocationSpeed = _gCount
		end
		if course ~= nil then
			_gHeaders[_gCount + 1] = "course"
			_gValues[_gCount + 1] = course
			_gCount = _gCount + 1
			_gLocationCourse = _gCount
		end
		if altitude ~= nil then
			_gHeaders[_gCount + 1] = "altitude"
			_gValues[_gCount + 1] = altitude
			_gCount = _gCount + 1
			_gLocationAltitude = _gCount
		end
		if radius ~= nil then
			_gHeaders[_gCount + 1] = "radius"
			_gValues[_gCount + 1] = radius
			_gCount = _gCount + 1
			_gLocationRadius = _gCount
		end

		local platform = tiros.moduledata.moduledata_get('framework','platform');
		if platform ~= nil then
			_gHeaders[_gCount + 1] = "platform"
			_gValues[_gCount + 1] = platform
			_gCount = _gCount + 1
		end
		local gender = tiros.moduledata.moduledata_get('framework','gender');
		if nil == gender then
			gender = "2"
		end
			_gHeaders[_gCount + 1] = "gender"
			_gValues[_gCount + 1] = tostring(gender)
			_gCount = _gCount + 1
			_gGender = _gCount
	else
		local uid = tiros.moduledata.moduledata_get('framework','uid');
		if uid == nil then
			uid = "";
		end
		_gHeaders[_gUid] = "uid"
		_gValues[_gUid] = uid
		local nlon,nlat,speed,course,altitude,radius = GetMyLocation()
		if nlon ~= nil then
			_gHeaders[_gLocationLon] = "selflon"
			_gValues[_gLocationLon] = nlon
		end
		if nlat ~= nil then
			_gHeaders[_gLocationLat] = "selflat"
			_gValues[_gLocationLat] = nlat
		end
		if speed ~= nil then
			_gHeaders[_gLocationSpeed] = "speed"
			_gValues[_gLocationSpeed] = speed
		end
		if course ~= nil then
			_gHeaders[_gLocationCourse] = "course"
			_gValues[_gLocationCourse] = course
		end
		if altitude ~= nil then
			_gHeaders[_gLocationAltitude] = "altitude"
			_gValues[_gLocationAltitude] = altitude
		end
		if radius ~= nil then
			_gHeaders[_gLocationRadius] = "radius"
			_gValues[_gLocationRadius] = radius
		end
		local gender = tiros.moduledata.moduledata_get('framework','gender');
		if nil == gender then
			gender = "2"
		end
		_gHeaders[_gGender] = "gender";
		_gValues[_gGender] = tostring(gender);
	end
end

--接口table
local interface = {}

--对外声明Headers count
--输出：Headers count
createmodule(interface,"httpheaderscount", function ()
	httpinit()
	return _gCount
end)

createmodule(interface,"httpgetheader", function (index)
	httpinit()
	if _gHeaders and _gValues then
		return _gValues[index],_gHeaders[index]
	else
		return nil,nil
	end
end)


tiros.nethttpheaders = readOnly(interface)

