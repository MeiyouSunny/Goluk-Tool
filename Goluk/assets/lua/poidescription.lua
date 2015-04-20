--根据poigid请求详细POI信息

--1 poidescriptionrequest(poigid,lon,lat,cbfunc,user)
--3 poidescriptionabort（）
--4 poidescriptiondestroy（）
--5 void*fn(const char* ptype, void* user, int16_t status,int32_t param1, const char* param2);

require"lua/positiondescription/sys_poidescription"
