package cn.com.mobnote.golukmobile.fileinfo;

import java.util.List;

public interface IVideoInfoDataFn {

	/**
	 * 往数据库中添加数据
	 * 
	 * @param bean
	 *            文件属性
	 * @author jyf
	 */
	public long addVideoInfoData(VideoFileInfoBean bean);

	/**
	 * 修改数据库中信息
	 * 
	 * @param bean
	 * @author jyf
	 */
	public void editVideoInfoData(VideoFileInfoBean bean);

	/**
	 * 删除数据库单条记录
	 * 
	 * @param fileName
	 *            文件名
	 * @author jyf
	 */
	public void delVideoInfo(String fileName);

	/**
	 * 按类型查询所有数据 (循环，紧急，精彩)
	 * 
	 * @param type
	 *            要查询的文件类型
	 * @author jyf
	 */
	public List<VideoFileInfoBean> selectAllData(String type);

	/**
	 * 通过文件名查询单个文件属性
	 * 
	 * @param fileName
	 * @return
	 * @author jyf
	 */
	public VideoFileInfoBean selectSingleData(String fileName);

	/**
	 * 关闭数据库
	 * 
	 * @author jyf
	 */
	public void destroy();

}
