package com.businessreviews.common;

import lombok.Getter;

import java.util.Random;

/**
 * 默认头像枚举
 * 提供系统预置的默认头像URL列表
 */
@Getter
public enum DefaultAvatar {
    /** 默认头像1 */
    AVATAR_1("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png"),
    /** 默认头像2 */
    AVATAR_2("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head2.png"),
    /** 默认头像3 */
    AVATAR_3("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head3.png"),
    /** 默认头像4 */
    AVATAR_4("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head4.png"),
    /** 默认头像5 */
    AVATAR_5("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head5.png"),
    /** 默认头像6 */
    AVATAR_6("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head6.png"),
    /** 默认头像7 */
    AVATAR_7("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png"),
    /** 默认头像8 */
    AVATAR_8("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head8.png"),
    /** 默认头像9 */
    AVATAR_9("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head9.png"),
    /** 默认头像10 */
    AVATAR_10("https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head10.png");
    
    private final String url;
    
    private static final Random RANDOM = new Random();
    
    DefaultAvatar(String url) {
        this.url = url;
    }
    
    /**
     * 随机获取一个默认头像URL
     * 
     * @return 随机头像URL
     */
    public static String getRandomAvatar() {
        DefaultAvatar[] avatars = DefaultAvatar.values();
        int index = RANDOM.nextInt(avatars.length);
        return avatars[index].getUrl();
    }
    
    /**
     * 获取所有头像URL数组
     * 
     * @return 头像URL数组
     */
    public static String[] getAllAvatarUrls() {
        DefaultAvatar[] avatars = DefaultAvatar.values();
        String[] urls = new String[avatars.length];
        for (int i = 0; i < avatars.length; i++) {
            urls[i] = avatars[i].getUrl();
        }
        return urls;
    }
}
