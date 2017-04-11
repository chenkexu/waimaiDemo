package mvp.circledemo.utils;


import com.cheikh.lazywaimai.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.bmob.v3.BmobObject;
import mvp.circledemo.bean.CircleItem;
import mvp.circledemo.bean.CommentItem;
import mvp.circledemo.bean.FavortItem;
import mvp.circledemo.bean.PhotoInfo;
import mvp.circledemo.bean.User;

/**
 * 
* @ClassName: DatasUtil 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author yiw
* @date 2015-12-28 下午4:16:21 
*
 */
public class DatasUtil {
	//内容
	public static final String[] CONTENTS = { "",
			"很新鲜，送货快，晚上再吃，好评",
			"很赞，非常方便，下次还会再买",
			"晚了一个一个小时。夜宵变早餐。配送员倒是没有什么，商家还撒谎说已经送出来了。这菜到手还是烫的，明明是刚做出来不就，不关配送员的事",
			"挂羊头卖狗肉，大家千万不要买，比正规的店铺贵一杯，假货",
			"非常好，非常喜欢，哈哈哈，么么，很便宜，建议大家去买，五分！！！！"
	  };
	//每个评论用户的头像
	public static final String[] HEADIMG = {
			"http://img.wzfzl.cn/uploads/allimg/140820/co140R00Q925-14.jpg",
			"http://www.feizl.com/upload2007/2014_06/1406272351394618.png",
			"http://v1.qzone.cc/avatar/201308/30/22/56/5220b2828a477072.jpg%21200x200.jpg",
			"http://v1.qzone.cc/avatar/201308/22/10/36/521579394f4bb419.jpg!200x200.jpg",
			"http://v1.qzone.cc/avatar/201408/20/17/23/53f468ff9c337550.jpg!200x200.jpg",
			"http://cdn.duitang.com/uploads/item/201408/13/20140813122725_8h8Yu.jpeg",
			"http://img.woyaogexing.com/touxiang/nv/20140212/9ac2117139f1ecd8%21200x200.jpg",
			"http://p1.qqyou.com/touxiang/uploadpic/2013-3/12/2013031212295986807.jpg"};

	public static List<User> users = new ArrayList<User>(); //所有的用户
	public static List<PhotoInfo> PHOTOS = new ArrayList<>(); //
	/**
	 * 动态id自增长
	 */
	private static int circleId = 0;
	/**
	 * 点赞id自增长
	 */
	private static int favortId = 0;
	/**
	 * 评论id自增长
	 */
	private static int commentId = 0;

	//当前的用户
	public static final User curUser = new User("0", "自己", HEADIMG[0]);

	public static User getCurUser() {
		return curUser;
	}

	private static List<CircleItem> circleDatas;


	public static void setCurUser(com.cheikh.lazywaimai.model.bean.User user) {
	     curUser.setHeadUrl(user.getAvatarUrl());
	     curUser.setId("0");
	     curUser.setName(user.getNickname());
	}

	//所有的用户
	static {
		User user1 = new User("1", "亚瑟", HEADIMG[1]);
		User user2 = new User("2", "鲁班七号", HEADIMG[2]);
		User user3 = new User("3", "不知火舞", HEADIMG[3]);
		User user4 = new User("4", "兰陵王", HEADIMG[4]);
		User user5 = new User("5", "后裔", HEADIMG[5]);
		User user6 = new User("6", "宫本武藏", HEADIMG[6]);
		User user7 = new User("7", "盲僧大大", HEADIMG[7]);

		users.add(curUser);
		users.add(user1);
		users.add(user2);
		users.add(user3);
		users.add(user4);
		users.add(user5);
		users.add(user6);
		users.add(user7);

//		List<BmobObject> list = new ArrayList<>();
//		for (int i = 0; i <users.size() ; i++) {
//			list.add(users.get(i));
//		}
//		new BmobBatch().insertBatch(list).doBatch(new QueryListListener<BatchResult>() {
//
//			@Override
//			public void done(List<BatchResult> list, BmobException e) {
//				if (e==null){
//					for(int i=0;i<list.size();i++){
//						BatchResult result = list.get(i);
//						BmobException ex =result.getError();
//						if(ex==null){
//							Logger.e("第"+i+"个数据批量添加成功："+result.getCreatedAt()+","+result.getObjectId()+","+result.getUpdatedAt());
//						}else{
//							Logger.e("第"+i+"个数据批量添加失败："+ex.getMessage()+","+ex.getErrorCode());
//						}
//					}
//				}else{
//					Logger.e("Bmob添加异常"+e.getMessage()+","+e.getErrorCode());
//				}
//			}
//		});


		//发布的照片
		PhotoInfo p1 = new PhotoInfo();
//		p1.url = "http://f.hiphotos.baidu.com/image/pic/item/faf2b2119313b07e97f760d908d7912396dd8c9c.jpg";
		p1.url = "http://i1.s1.dpfile.com/pc/f59ce7b879eea202f36692aa9ead9dac(249x249)/thumb.jpg";
		p1.w = 640;
		p1.h = 792;

		PhotoInfo p2 = new PhotoInfo();
//		p2.url = "http://g.hiphotos.baidu.com/image/pic/item/4b90f603738da977c76ab6fab451f8198718e39e.jpg";
		p2.url = "http://p0.meituan.net/ugcpic/f59ce7b879eea202f36692aa9ead9dac%40249w_249h_1e_1l%7Cwatermark%3D1%26%26r%3D1%26p%3D9%26x%3D2%26y%3D2%26relative%3D1%26o%3D20";
		p2.w = 640;
		p2.h = 792;

		PhotoInfo p3 = new PhotoInfo();
//		p3.url = "http://e.hiphotos.baidu.com/image/pic/item/902397dda144ad343de8b756d4a20cf430ad858f.jpg";
		p3.url = "http://i3.dpfile.com/2008-10-05/1014416_m.jpg";
		p3.w = 950;
		p3.h = 597;

		PhotoInfo p4 = new PhotoInfo();
//		p4.url = "http://a.hiphotos.baidu.com/image/pic/item/a6efce1b9d16fdfa0fbc1ebfb68f8c5495ee7b8b.jpg";
		p4.url = "http://i2.dpfile.com/2008-01-20/314677_m.jpg";
		p4.w = 533;
		p4.h = 800;

		PhotoInfo p5 = new PhotoInfo();
//		p5.url = "http://b.hiphotos.baidu.com/image/pic/item/a71ea8d3fd1f4134e61e0f90211f95cad1c85e36.jpg";
		p5.url = "http://i1.dpfile.com/2011-01-22/6576762_m.jpg";
		p5.w = 700;
		p5.h = 467;

		PhotoInfo p6 = new PhotoInfo();
		p6.url = "http://i2.dpfile.com/2009-04-24/1916533_m.jpg";
		p6.w = 700;
		p6.h = 467;

		PhotoInfo p7 = new PhotoInfo();
		p7.url = "http://i1.dpfile.com/2008-11-22/1209528_m.jpg";
		p7.w = 1024;
		p7.h = 640;

		PhotoInfo p8 = new PhotoInfo();
		p8.url = "http://pic4.nipic.com/20091101/3672704_160309066949_2.jpg";
		p8.w = 1024;
		p8.h = 768;

		PhotoInfo p9 = new PhotoInfo();
		p9.url = "http://i3.dpfile.com/2009-11-04/3080111_m.jpg";
		p9.w = 1024;
		p9.h = 640;

		PhotoInfo p10 = new PhotoInfo();
		p10.url = "http://i2.dpfile.com/2009-04-24/1916533_m.jpg";
		p10.w = 1024;
		p10.h = 768;

		PHOTOS.add(p1);
		PHOTOS.add(p2);
		PHOTOS.add(p3);
		PHOTOS.add(p4);
		PHOTOS.add(p5);
		PHOTOS.add(p6);
		PHOTOS.add(p7);
		PHOTOS.add(p8);
		PHOTOS.add(p9);
		PHOTOS.add(p10);
	}





	/**
	 * 创建一个说说
	 */
	public static void addCircleItem(CircleItem circleItem){
		circleDatas.add(circleItem);
	}

	public static List<CircleItem> getCircleDatas() {
		return circleDatas;
	}



	/**
	 * 创建所有的说说
	 * @return
     */
	public static List<CircleItem> createCircleDatas() {
		circleDatas = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			CircleItem item = new CircleItem();
			User user = getUser();
			item.setId(String.valueOf(circleId++));
			item.setUser(user);
			item.setContent(getContent());
			item.setCreateTime(DateUtil.getDate(new Date()));
			item.setFavorters(createFavortItemList());
			item.setComments(createCommentItemList());
            item.setRating(2);
			int type = getRandomNum(10) % 2;//说说的类型
			if (type == 0) {
//				item.setType("1");// 链接
//				item.setLinkImg("http://pics.sc.chinaz.com/Files/pic/icons128/2264/%E8%85%BE%E8%AE%AFQQ%E5%9B%BE%E6%A0%87%E4%B8%8B%E8%BD%BD1.png");
//				item.setLinkTitle("百度一下，你就知道");
				item.setType("2");// 图片
				item.setPhotos(createPhotos());
			} else if(type == 1){
				item.setType("2");// 图片
				item.setPhotos(createPhotos());
			}else {
				item.setType("3");// 视频
				String videoUrl = "http://yiwcicledemo.s.qupai.me/v/80c81c19-7c02-4dee-baca-c97d9bbd6607.mp4";
                String videoImgUrl = "http://yiwcicledemo.s.qupai.me/v/80c81c19-7c02-4dee-baca-c97d9bbd6607.jpg";
				item.setVideoUrl(videoUrl);
                item.setVideoImgUrl(videoImgUrl);
			}
			circleDatas.add(item);
		}

		//保存到数据库，
		List<BmobObject> list = new ArrayList<>();
		for (int i = 0; i < circleDatas.size() ; i++) {
			list.add(circleDatas.get(i));
		}
//
//		new BmobBatch().insertBatch(list).doBatch(new QueryListListener<BatchResult>() {
//
//			@Override
//			public void done(List<BatchResult> list, BmobException e) {
//				if (e==null){
//					for(int i=0;i<list.size();i++){
//						BatchResult result = list.get(i);
//						BmobException ex =result.getError();
//						if(ex==null){
//							Logger.e("第"+i+"个数据批量添加成功："+result.getCreatedAt()+","+result.getObjectId()+","+result.getUpdatedAt());
//						}else{
//							Logger.e("第"+i+"个数据批量添加失败："+ex.getMessage()+","+ex.getErrorCode());
//						}
//					}
//				}else{
//					Logger.e("Bmob添加异常"+e.getMessage()+","+e.getErrorCode());
//				}
//			}
//		});
		return circleDatas;
	}

	//获取一个用户
	public static User getUser() {
		return users.get(getRandomNum(users.size()));
	}

	/**
	 * 获取内容
	 * @return
	 */
	public static String getContent() {
		return CONTENTS[getRandomNum(CONTENTS.length)];
	}


	public static int getRandomNum(int max) {
		Random random = new Random();
		int result = random.nextInt(max);
		return result;
	}

    /**
	 * 创建发表的图片的内容
	 * @return
	 */
	public static List<PhotoInfo> createPhotos() {
		List<PhotoInfo> photos = new ArrayList<>();
		int size = getRandomNum(PHOTOS.size());
		if (size > 0) {
			if (size > 9) {
				size = 9;
			}
			for (int i = 0; i < size; i++) {
				PhotoInfo photo = PHOTOS.get(getRandomNum(PHOTOS.size()));
				if (!photos.contains(photo)) {
					photos.add(photo);
				} else {
					i--;
				}
			}
		}
		return photos;
	}

	/**
	 * 展示点赞的列表
	 * @return
     */
	public static List<FavortItem> createFavortItemList() {
		int size = getRandomNum(users.size());//随机获取一个长度
		List<FavortItem> items = new ArrayList<>();
		List<String> history = new ArrayList<>();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				FavortItem newItem = createFavortItem();//创建一个赞
				String userid = newItem.getUser().getId();//获取赞的userId.
				if (!history.contains(userid)) { 		//如果该id不存在
					items.add(newItem);
					history.add(userid);
				} else {
					i--;
				}
			}
		}
		return items;
	}

	/**
	 * 创建一个赞
	 * @return
     */
	public static FavortItem createFavortItem() {
		FavortItem item = new FavortItem();
		item.setId(String.valueOf(favortId++));
		item.setUser(getUser());
		return item;
	}

	/**
	 * 增加一个当前的用户最喜欢的说说的item
	 * @return
     */
	public static FavortItem createCurUserFavortItem() {
		FavortItem item = new FavortItem();
		item.setId(String.valueOf(favortId++));
		item.setUser(curUser);
		return item;
	}

	/**
	 * 创建所有的评论列表
	 * @return
     */
	public static List<CommentItem> createCommentItemList() {
		List<CommentItem> items = new ArrayList<>();
		int size = getRandomNum(10);
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				items.add(createComment());
			}
		}
		return items;
	}

	/**
	 * 发表评论
	 * @return
     */
	public static CommentItem createComment() {
		CommentItem item = new CommentItem();
		item.setId(String.valueOf(commentId++));
		item.setContent("哈哈");
		User user = getUser();
		item.setUser(user);
		if (getRandomNum(10) % 2 == 0) {
			while (true) {
				User replyUser = getUser();
				if (!user.getId().equals(replyUser.getId())) {
					item.setToReplyUser(replyUser);
					break;
				}
			}
		}
		return item;
	}
	
	/**
	 * 创建发布评论
	 * pram 评论的内容
	 * @return
	 */
	public static CommentItem createPublicComment(String content){
		CommentItem item = new CommentItem();
		item.setId(String.valueOf(commentId++));
		item.setContent(content);
		item.setUser(curUser); //发表评论的当前的用户
		return item;
	}
	
	/**
	 * 创建回复评论（回复的用户）
	 * @return
	 */
	public static CommentItem createReplyComment(User replyUser, String content){
		CommentItem item = new CommentItem();
		item.setId(String.valueOf(commentId++));
		item.setContent(content);
		item.setUser(curUser);
		item.setToReplyUser(replyUser);
		return item;
	}


}
