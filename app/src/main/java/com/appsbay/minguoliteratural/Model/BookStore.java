package com.appsbay.minguoliteratural.Model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class BookStore {
    public static BookStore shared = new BookStore();

    ArrayList<Book> booksChineseSimple = new ArrayList<Book>() {{
// 沈从文
        add(new Book("边城", "边城", "沈从文", "边城", BookType.shenCongWen));
        add(new Book("老实人", "老实人", "沈从文", "老实人", BookType.shenCongWen));
        add(new Book("长河", "长河", "沈从文", "长河", BookType.shenCongWen));
        add(new Book("蜜柑", "蜜柑", "沈从文", "蜜柑", BookType.shenCongWen));
        add(new Book("山鬼", "山鬼", "沈从文", "山鬼", BookType.shenCongWen));
        add(new Book("鸭子", "鸭子", "沈从文", "鸭子", BookType.shenCongWen));
// 鲁迅
        add(new Book("彷徨", "彷徨", "鲁迅", "彷徨", BookType.luXun));
        add(new Book("呐喊", "呐喊", "鲁迅", "呐喊", BookType.luXun));
        add(new Book("故事新编", "故事新编", "鲁迅", "故事新编", BookType.luXun));
        add(new Book("朝花夕拾", "朝花夕拾", "鲁迅", "朝花夕拾", BookType.luXun));
        add(new Book("野草", "野草", "鲁迅", "野草", BookType.luXun));
        add(new Book("原野", "原野", "鲁迅", "原野", BookType.luXun));
        add(new Book("华盖集", "华盖集", "鲁迅", "华盖集", BookType.luXun));
// 张恨水
        add(new Book("金粉世家", "金粉世家", "张恨水", "金粉世家", BookType.zhangHenShui));
        add(new Book("啼笑姻缘", "啼笑姻缘", "张恨水", "啼笑姻缘", BookType.zhangHenShui));
        add(new Book("啼笑姻缘续集", "啼笑姻缘续集", "张恨水", "啼笑姻缘续集", BookType.zhangHenShui));
        add(new Book("张恨水散文", "张恨水散文", "张恨水", "张恨水散文", BookType.zhangHenShui));
        add(new Book("美人恩", "美人恩", "张恨水", "美人恩", BookType.zhangHenShui));
        add(new Book("巴山夜雨", "巴山夜雨", "张恨水", "巴山夜雨", BookType.zhangHenShui));
        add(new Book("欢喜冤家", "欢喜冤家", "张恨水", "欢喜冤家", BookType.zhangHenShui));
        add(new Book("大江东去", "大江东去", "张恨水", "大江东去", BookType.zhangHenShui));
        add(new Book("春明外史", "春明外史", "张恨水", "春明外史", BookType.zhangHenShui));
// 巴金
        add(new Book("爱情三部曲 电", "爱情三部曲 电", "巴金", "爱情三部曲 电", BookType.baJin));
        add(new Book("爱情三部曲 雨", "爱情三部曲 雨", "巴金", "爱情三部曲 雨", BookType.baJin));
        add(new Book("爱情三部曲 雾", "爱情三部曲 雾", "巴金", "爱情三部曲 雾", BookType.baJin));
        add(new Book("雷", "雷", "巴金", "雷", BookType.baJin));
        add(new Book("家", "家", "巴金", "家", BookType.baJin));
        add(new Book("春", "春", "巴金", "春", BookType.baJin));
        add(new Book("秋", "秋", "巴金", "秋", BookType.baJin));
// 其他
        add(new Book("围城", "围城", "钱钟书", "围城", BookType.other));
        add(new Book("写在人生边上", "写在人生边上", "钱钟书", "写在人生边上", BookType.other));
        add(new Book("雷雨", "雷雨", "曹禺", "雷雨", BookType.other));
        add(new Book("茶馆", "茶馆", "老舍", "茶馆", BookType.other));
        add(new Book("骆驼祥子", "骆驼祥子", "老舍", "骆驼祥子", BookType.other));
        add(new Book("倾城之恋", "倾城之恋", "张爱玲", "倾城之恋", BookType.other));
        add(new Book("张爱玲散文", "张爱玲散文", "张爱玲", "张爱玲散文", BookType.other));
        add(new Book("红楼梦魇", "红楼梦魇", "张爱玲", "红楼梦魇", BookType.other));
        add(new Book("子夜", "子夜", "茅盾", "子夜", BookType.other));
        add(new Book("京华烟云", "京华烟云", "林语堂", "京华烟云", BookType.other));
        add(new Book("丁玲中短篇作品", "丁玲中短篇作品", "丁玲", "丁玲中短篇作品", BookType.other));
        add(new Book("你是人间四月天", "你是人间四月天", "林徽因", "你是人间四月天", BookType.other));
        add(new Book("关于女人", "关于女人", "冰心", "关于女人", BookType.other));
        add(new Book("林语堂散文", "林语堂散文", "林语堂", "林语堂散文", BookType.other));
        add(new Book("石头记索隐", "石头记索隐", "蔡元培", "石头记索隐", BookType.other));
        add(new Book("人·兽·鬼", "人·兽·鬼", "钱钟书", "人·兽·鬼", BookType.other));
        add(new Book("倪焕之", "倪焕之", "叶圣陶", "倪焕之", BookType.other));
        add(new Book("呼兰河传", "呼兰河传", "萧红", "呼兰河传", BookType.other));
        add(new Book("四世同堂", "四世同堂", "老舍", "四世同堂", BookType.other));
        add(new Book("生活的艺术", "生活的艺术", "林语堂", "生活的艺术", BookType.other));
        add(new Book("稻草人", "稻草人", "叶圣陶", "稻草人", BookType.other));
        add(new Book("迷羊", "迷羊", "郁达夫", "迷羊", BookType.other));
        add(new Book("人生随感", "人生随感", "徐志摩", "人生随感", BookType.other));
        add(new Book("高山氤氲", "高山氤氲", "徐志摩", "高山氤氲", BookType.other));
        add(new Book("云游心踪", "云游心踪", "徐志摩", "云游心踪", BookType.other));
        add(new Book("我这一辈子", "我这一辈子", "老舍", "我这一辈子", BookType.other));
        add(new Book("风雨故人", "风雨故人", "徐志摩", "风雨故人", BookType.other));
        add(new Book("小城三月", "小城三月", "萧红", "小城三月", BookType.other));
        add(new Book("徐志摩诗集", "徐志摩诗集", "徐志摩", "徐志摩诗集", BookType.other));

    }};

    ArrayList<Book> booksChineseTraditional = new ArrayList<Book>() {{
// 沈從文
        add(new Book("边城", "邊城", "沈從文", "邊城", BookType.shenCongWen_Fan));
        add(new Book("老实人", "老實人", "沈從文", "老實人", BookType.shenCongWen_Fan));
        add(new Book("长河", "長河", "沈從文", "長河", BookType.shenCongWen_Fan));
        add(new Book("蜜柑", "蜜柑 (繁體)", "沈從文", "蜜柑繁体", BookType.shenCongWen_Fan));
        add(new Book("山鬼", "山鬼 (繁體)", "沈從文", "山鬼繁体", BookType.shenCongWen_Fan));
        add(new Book("鸭子", "鴨子", "沈從文", "鴨子", BookType.shenCongWen_Fan));
// 魯迅
        add(new Book("彷徨", "彷徨 (繁體)", "魯迅", "彷徨繁体", BookType.luXun_Fan));
        add(new Book("呐喊", "吶喊", "魯迅", "吶喊", BookType.luXun_Fan));
        add(new Book("故事新编", "故事新編", "魯迅", "故事新編", BookType.luXun_Fan));
        add(new Book("朝花夕拾", "朝花夕拾繁体", "魯迅", "朝花夕拾", BookType.luXun_Fan));
        add(new Book("野草", "野草 (繁體)", "魯迅", "野草_f", BookType.luXun_Fan));
        add(new Book("原野", "原野 (繁體)", "魯迅", "原野_f", BookType.luXun_Fan));
        add(new Book("华盖集", "華蓋集", "魯迅", "华盖集_f", BookType.luXun_Fan));
// 張恨水
        add(new Book("金粉世家", "金粉世家 (繁體)", "张恨水", "金粉世家_f", BookType.zhangHenShui_Fan));
        add(new Book("啼笑姻缘", "啼笑姻緣", "張恨水", "啼笑姻緣", BookType.zhangHenShui_Fan));
        add(new Book("啼笑姻缘续集", "啼笑姻緣續集", "張恨水", "啼笑姻緣續集", BookType.zhangHenShui_Fan));
        add(new Book("张恨水散文", "張恨水散文", "張恨水", "張恨水散文", BookType.zhangHenShui_Fan));
        add(new Book("美人恩", "美人恩 (繁體)", "張恨水", "美人恩繁体", BookType.zhangHenShui_Fan));
        add(new Book("巴山夜雨", "巴山夜雨 (繁體)", "張恨水", "巴山夜雨_f", BookType.zhangHenShui_Fan));
        add(new Book("欢喜冤家", "歡喜冤家", "張恨水", "欢喜冤家_f", BookType.zhangHenShui_Fan));
        add(new Book("大江东去", "大江東去", "張恨水", "大江东去_f", BookType.zhangHenShui_Fan));
        add(new Book("春明外史", "春明外史 (繁體)", "張恨水", "春明外史_f", BookType.zhangHenShui_Fan));
// 巴金
        add(new Book("爱情三部曲 电", "愛情三部曲 電", "巴金", "愛情三部曲 電", BookType.baJin_Fan));
        add(new Book("爱情三部曲 雨", "愛情三部曲 雨", "巴金", "愛情三部曲 雨", BookType.baJin_Fan));
        add(new Book("爱情三部曲 雾", "愛情三部曲 霧", "巴金", "愛情三部曲 霧", BookType.baJin_Fan));
        add(new Book("雷", "雷 (繁體)", "巴金", "雷繁体", BookType.baJin_Fan));
        add(new Book("家", "家 (繁體)", "巴金", "家_f", BookType.baJin_Fan));
        add(new Book("春", "春 (繁體)", "巴金", "春_f", BookType.baJin_Fan));
        add(new Book("秋", "秋 (繁體)", "巴金", "秋_f", BookType.baJin_Fan));
// 其他
        add(new Book("围城", "圍城", "錢鍾書", "圍城", BookType.other_Fan));
        add(new Book("写在人生边上", "寫在人生邊上", "錢鍾書", "写在人生边上_f", BookType.other_Fan));
        add(new Book("雷雨", "雷雨 (繁體)", "曹禺", "雷雨繁体", BookType.other_Fan));
        add(new Book("茶馆", "茶館", "老舍", "茶館", BookType.other_Fan));
        add(new Book("骆驼祥子", "駱駝祥子", "老舍", "骆驼祥子_f", BookType.other_Fan));
        add(new Book("倾城之恋", "傾城之戀", "張愛玲", "傾城之戀", BookType.other_Fan));
        add(new Book("张爱玲散文", "張愛玲散文", "張愛玲", "张爱玲散文_f", BookType.other_Fan));
        add(new Book("红楼梦魇", "紅樓夢魘", "張愛玲", "红楼梦魇_f", BookType.other_Fan));
        add(new Book("子夜", "子夜 (繁體)", "茅盾", "子夜_f", BookType.other_Fan));
        add(new Book("京华烟云", "京華煙雲", "林語堂", "京华烟云_f", BookType.other_Fan));
        add(new Book("丁玲中短篇作品", "丁玲中短篇作品 (繁體)", "丁玲", "丁玲中短篇作品_f", BookType.other_Fan));
        add(new Book("你是人间四月天", "你是人間四月天", "林徽因", "你是人间四月天_f", BookType.other_Fan));
        add(new Book("关于女人", "關於女人", "冰心", "关于女人_f", BookType.other_Fan));
        add(new Book("林语堂散文", "林語堂散文", "林語堂", "林语堂散文_f", BookType.other_Fan));
        add(new Book("石头记索隐", "石頭記索隱", "蔡元培", "石头记索隐_f", BookType.other_Fan));
        add(new Book("人·兽·鬼", "人·獸·鬼", "錢鍾書", "人·兽·鬼_f", BookType.other_Fan));
        add(new Book("倪焕之", "倪煥之", "葉聖陶", "倪焕之_f", BookType.other_Fan));
        add(new Book("呼兰河传", "呼蘭河傳", "蕭紅", "呼兰河传_f", BookType.other_Fan));
        add(new Book("四世同堂", "四世同堂 (繁體)", "老舍", "四世同堂_f", BookType.other_Fan));
        add(new Book("生活的艺术", "生活的藝術", "林語堂", "生活的艺术_f", BookType.other_Fan));
        add(new Book("稻草人", "稻草人 (繁體)", "葉聖陶", "稻草人_f", BookType.other_Fan));
        add(new Book("迷羊", "迷羊 (繁體)", "鬱達夫", "迷羊_f", BookType.other_Fan));
        add(new Book("人生随感", "人生隨感", "徐志摩", "人生随感_f", BookType.other_Fan));
        add(new Book("高山氤氲", "高山氤氲 (繁體)", "徐志摩", "高山氤氲_f", BookType.other_Fan));
        add(new Book("云游心踪", "雲遊心踪", "徐志摩", "云游心踪_f", BookType.other_Fan));
        add(new Book("我这一辈子", "我這一輩子", "老舍", "我这一辈子_f", BookType.other_Fan));
        add(new Book("风雨故人", "風雨故人", "徐志摩", "风雨故人_f", BookType.other_Fan));
        add(new Book("小城三月", "小城三月 (繁體)", "蕭紅", "小城三月_f", BookType.other_Fan));
        add(new Book("徐志摩诗集", "徐志摩詩集", "徐志摩", "徐志摩诗集_f", BookType.other_Fan));
    }};

    public ArrayList<Book> getBooks(Context context) {
        ArrayList<Book> books;

        SharedPreferences preferences = context.getSharedPreferences("Language Preference", Context.MODE_PRIVATE);
        int language = preferences.getInt("language", 0);

        if (language == 0) {
            books = BookStore.shared.booksChineseSimple;
        } else {
            books = BookStore.shared.booksChineseTraditional;
        }

        return books;
    }

}
