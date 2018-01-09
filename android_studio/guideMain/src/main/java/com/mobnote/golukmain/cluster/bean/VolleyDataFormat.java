package com.mobnote.golukmain.cluster.bean;

import com.mobnote.golukmain.newest.CommentDataInfo;
import com.mobnote.golukmain.videosuqare.LiveVideoData;
import com.mobnote.golukmain.videosuqare.UserEntity;
import com.mobnote.golukmain.videosuqare.VideoEntity;
import com.mobnote.golukmain.videosuqare.VideoExtra;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;

import java.util.ArrayList;
import java.util.List;

public class VolleyDataFormat {

    /**
     * 获取视频列表数据
     */
    public List<VideoSquareInfo> getClusterList(List<TagGeneralVideoListBean> list) {
        List<VideoSquareInfo> clusters = null;

        if (null == list || list.size() == 0) {
            return null;
        }

        clusters = new ArrayList<VideoSquareInfo>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < list.size(); i++) {
            TagGeneralVideoListBean videoinfo = list.get(i);
            if (null != videoinfo) {
                VideoEntity mVideoEntity = new VideoEntity();
                TagGeneralVideoBean video = videoinfo.video;
                if (null != video) {
                    LiveVideoData lvd = new LiveVideoData();
                    TagGeneralVideoDataBean live = video.videodata;
                    if (null != live) {
                        lvd.active = live.active;
                        lvd.aid = live.aid;
                        lvd.flux = live.flux;
                        lvd.lat = live.lat;
                        lvd.lon = live.lon;
                        lvd.mid = live.mid;
                        lvd.open = live.open;
                        lvd.restime = live.restime;
                        lvd.speed = live.speed;
                        lvd.tag = live.tag;
                        lvd.talk = live.talk;
                        lvd.vtype = live.vtype;
                    }
                    mVideoEntity.category = video.category;
                    mVideoEntity.videoid = video.videoid;
                    mVideoEntity.type = video.type;
                    mVideoEntity.sharingtime = video.sharingtime;
                    mVideoEntity.sharingts = video.sharingts;
                    mVideoEntity.describe = video.describe;
                    mVideoEntity.picture = video.picture;
                    mVideoEntity.clicknumber = video.clicknumber;
                    mVideoEntity.praisenumber = video.praisenumber;
                    mVideoEntity.starttime = video.starttime;
                    mVideoEntity.livetime = video.livetime;
                    mVideoEntity.livewebaddress = video.livewebaddress;
                    mVideoEntity.livesdkaddress = video.livesdkaddress;
                    mVideoEntity.ondemandwebaddress = video.ondemandwebaddress;
                    mVideoEntity.ondemandsdkaddress = video.ondemandsdkaddress;
                    mVideoEntity.ispraise = video.ispraise;
                    mVideoEntity.livevideodata = lvd;
                    mVideoEntity.location = video.location;
                    mVideoEntity.reason = video.reason;
                    mVideoEntity.isopen = "0";
                    mVideoEntity.tags = video.tags;

                    TagGeneralGenBean gb = video.gen;
                    if (gb != null) {
                        VideoExtra ve = new VideoExtra();
                        ve.topicid = gb.topicid;
                        ve.channelid = gb.channelid;
                        ve.isrecommend = gb.isrecommend;
                        ve.isreward = gb.isreward;
                        ve.topicname = gb.topicname;
                        ve.atflag = gb.atflag;
                        ve.sysflag = gb.sysflag;
                        mVideoEntity.videoExtra = ve;
                    }

                    TagGeneralCommentBean comment = video.comment;
                    if (null != comment) {
                        mVideoEntity.iscomment = comment.iscomment;
                        mVideoEntity.comcount = comment.comcount;

                        List<TagGeneralComListBean> comlist = comment.comlist;
                        if (null != comlist) {
                            for (int j = 0; j < comlist.size(); j++) {
                                TagGeneralComListBean item = comlist.get(j);
                                CommentDataInfo comminfo = new CommentDataInfo();

                                comminfo.commentid = item.commentid;
                                comminfo.authorid = item.authorid;
                                comminfo.name = item.name;
                                comminfo.avatar = item.avatar;
                                comminfo.time = item.text;
                                comminfo.text = item.text;
                                comminfo.replyid = item.replyid;
                                comminfo.replyname = item.replyname;
                                mVideoEntity.commentList.add(comminfo);
                            }
                        }
                    }

                }

                UserEntity mUserEntity = new UserEntity();
                TagGeneralUserBean user = videoinfo.user;
                if (null != user) {
                    mUserEntity.uid = user.uid;
                    mUserEntity.nickname = user.nickname;
                    mUserEntity.headportrait = user.headportrait;
                    mUserEntity.sex = user.sex;
                    mUserEntity.mCustomAvatar = user.customavatar;
//						mUserEntity.label = user.label;
                    UserLabelBean labelBean = new UserLabelBean();
                    labelBean.approve = user.label.approve;
                    labelBean.approvelabel = user.label.approvelabel;
                    labelBean.headplusv = user.label.headplusv;
                    labelBean.headplusvdes = user.label.headplusvdes;
                    labelBean.tarento = user.label.tarento;
                    mUserEntity.label = labelBean;
//						mUserEntity.link = user.link;
                }

                long id = time + i;
                VideoSquareInfo mVideoSquareInfo = new VideoSquareInfo();
                mVideoSquareInfo.mVideoEntity = mVideoEntity;
                mVideoSquareInfo.mUserEntity = mUserEntity;
                mVideoSquareInfo.id = "" + id;
                clusters.add(mVideoSquareInfo);
            }
        }
        return clusters;
    }

}
