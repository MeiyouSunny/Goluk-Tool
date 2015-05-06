package cn.com.mobnote.golukmobile.multicast;


/**
 * @描述：IP摄像头请求
 * @作者： hanzheng
 * @时间： 2015年1月15日 下午4:23:44
 */
public class RequestVdcpBean {

	public Integer getPmask1() {
		return pmask1;
	}

	public void setPmask1(Integer pmask1) {
		this.pmask1 = pmask1;
	}

	public String getSername() {
		return sername;
	}

	public void setSername(String sername) {
		this.sername = sername;
	}

	public Short getNtypemain() {
		return ntypemain;
	}

	public void setNtypemain(Short ntypemain) {
		this.ntypemain = ntypemain;
	}

	public Short getNtypesub() {
		return ntypesub;
	}

	public void setNtypesub(Short ntypesub) {
		this.ntypesub = ntypesub;
	}

	public String getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public Integer getNchannel() {
		return nchannel;
	}

	public void setNchannel(Integer nchannel) {
		this.nchannel = nchannel;
	}

	public Integer getDwdatasize() {
		return dwdatasize;
	}

	public void setDwdatasize(Integer dwdatasize) {
		this.dwdatasize = dwdatasize;
	}

	public Integer getPmask2() {
		return pmask2;
	}

	public void setPmask2(Integer pmask2) {
		this.pmask2 = pmask2;
	}

	Integer pmask1 = null;
	String sername = null;
	Short ntypemain = null;

	public Byte getVersion() {
		return version;
	}

	public void setVersion(Byte version) {
		this.version = version;
	}

	Short ntypesub = null;
	Byte version = null;
	String res = null;
	Integer nchannel = null;
	Integer dwdatasize = null;
	Integer pmask2 = null;

	// int pMask1; /**< MSGHEAD_MASK1 */
	// char sername[24]; /**< 服务器名称，此项由发起端指定，服务端不验证 */
	// short nTypeMain; /**< 消息 ID，根据不同 ID 后面的数据类型不同 */
	// short nTypeSub; /**< 附加变量，返回时用于判断返回值 */
	// char version; /**< 设备发现协议(VDDP)版本号，目前定义了两个版本,分别取值为 0 和1 */
	// char res[3]; /**< 保留 */
	// int nChannel; /**< 通道数量 */
	// int dwDataSize; /**< 数据长度，不包含数据头长度 */
	// int pMask2; /**< MSGHEAD_MASK2 */

	public byte[] makeSendByte() {
		// 初始化总的数组。
		byte[] rsByte = new byte[48];
		// IpcEnum ipcEnum = new IpcEnum();
		byte[] temp = null;
		int count = 0;
		// pmask1 4
		Integer pmask1 = this.getPmask1();
		if (pmask1 != null) {
			temp = ByteUtil.int2Bytes(pmask1);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// sername 24
		String sername = this.getSername();
		if (sername != null) {
			temp = ByteUtil.string2Bytes(sername, 24);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// nTypeMain 获得类型 2字节
		Short ntypemain = this.getNtypemain();
		if (ntypemain != null) {
			temp = ByteUtil.short2Bytes(ntypemain);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// nTypeSub 获得类型 2字节
		Short ntypesub = this.getNtypesub();
		if (ntypesub != null) {
			temp = ByteUtil.short2Bytes(ntypesub);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// 加密类型 1字节
		Byte version = this.getVersion();
		if (version != null) {
			temp = new byte[1];
			temp[0] = version;
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}

		// res 3
		String res = this.getRes();
		if (res != null) {
			temp = ByteUtil.string2Bytes(res, 3);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}

		// 4
		Integer nchannel = this.getNchannel();
		if (nchannel != null) {
			temp = ByteUtil.int2Bytes(nchannel);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// 4
		Integer dwdatasize = this.getDwdatasize();
		if (dwdatasize != null) {
			temp = ByteUtil.int2Bytes(dwdatasize);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// 4
		Integer pmask2 = this.getPmask2();
		if (pmask2 != null) {
			temp = ByteUtil.int2Bytes(pmask2);
			System.arraycopy(temp, 0, rsByte, count, temp.length);
			count = count + temp.length;
			temp = null;
		}
		// byte[] dataByte = this.getData();
		// if (dataByte != null) {
		//
		// System.arraycopy(dataByte, 0, rsByte, count, dataByte.length);
		// count = count + dataByte.length;
		// dataByte = null;
		// }
		/**
		 * // 1字节 Byte obligate1Byte = this.getObligate1(); // 1字节 Byte
		 * obligate2Byte = this.getObligate2(); // 1字节 Byte obligate3Byte =
		 * this.getObligate3(); // 1字节 Byte obligate4Byte = this.getObligate4();
		 **/

		return rsByte;

	}

}
