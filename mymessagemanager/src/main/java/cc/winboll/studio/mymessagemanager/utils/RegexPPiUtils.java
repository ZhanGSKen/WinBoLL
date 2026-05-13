package cc.winboll.studio.mymessagemanager.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/09 11:10
 * @Describe  .* 前置预防针
 * regex pointer preventive injection
 * 简称 RegexPPi
 */
public class RegexPPiUtils {

	public static final String TAG = "RegexPPiUtils";

	//
	// 检验文本是否满足适合正则表达式模式计算
	//
	public static boolean isPPiOK(String text) {
		//String text = "这里是一些任意的文本内容";
		String regex = ".*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		/*if (matcher.matches()) {
		 System.out.println("文本满足该正则表达式模式");
		 } else {
		 System.out.println("文本不满足该正则表达式模式");
		 }*/
		return matcher.matches();
	}
}
