package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	//商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	//商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "のフォーマットが不正です";
	private static final String SALEFILE_INVALID_FORMAT = "売上ファイルのフォーマットが不正です";
	private static final String NOT_CONSECUTIVE_NUMBERS = "売上ファイル名が連番ではありません";
	private static final String TOTAL_AMOUNT_OVERFLOW = "合計金額が10桁を超えました";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//【argsチェック】
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, "^[0-9]{3}$", "支店定義ファイル", branchNames, branchSales)) {
			return;
		}

		// 商品定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODITY_LST, "^[0-9A-Za-z]{8}$", "商品定義ファイル", commodityNames, commoditySales)) {
			return;
		}

		// 処理内容2-1. 売上ファイル検索
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			String fileName = files[i].getName() ;

			if(files[i].isFile() && fileName.matches("^[0-9]{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		Collections.sort(rcdFiles);

		//エラー処理2-1.　売上ファイル名 連番チェック
		for(int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i ++).getName().substring(0, 8));

			if((latter - former) != 1) {
				System.out.println(NOT_CONSECUTIVE_NUMBERS);
				return;
			}

		}

		//処理内容2-2 売上ファイル読込処理
		BufferedReader br = null;

		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				br = new BufferedReader(new FileReader(rcdFiles.get(i)));
				ArrayList<String> fileContents = new ArrayList<>();

				String line;
				// 一行ずつ読み込む
				while((line = br.readLine()) != null) {
					fileContents.add(line);
				}

				//【行数チェック】
				if(fileContents.size() != 3) {
					System.out.println(SALEFILE_INVALID_FORMAT);
				}

				//支店名を取得(売上集計ファイルの中身＝branchで1行、Saleで1行の計2行を記録)
				String branchCode = fileContents.get(0);

				//【支店コードの存在チェック】↑の中身が支店定義ファイルにあるかチェック
				if (!branchSales.containsKey(branchCode)) {
					System.out.println("支店コード " + branchCode + " は支店定義ファイルに存在しません。");
				}

				//【売上金額-数字チェック】
				if(!fileContents.get(2).matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//型の変換
				//ファイルから読み込んだ情報は、内容にかかわらず一律でStringとして扱われます
				long fileSale = Long.parseLong(fileContents.get(2));

				//読み込んだ売上⾦額(fileSale)を加算
				Long saleAmount = branchSales.get(branchCode) + fileSale;

				//【桁数溢れチェック】
				if(saleAmount >= 10000000000L){
					System.out.println(TOTAL_AMOUNT_OVERFLOW);
				}
				//Map追加
				branchSales.put(branchCode, saleAmount);

			}catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 処理内容3-1. 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品定義で追加. 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, String regex,String type, Map<String, String> names, Map<String, Long> sales) {
		BufferedReader br = null;  //これってtryの外で宣言してる理由とかがあったんだっけ　→　Finarryでも使ってるからでは。

		try {
			File file = new File(path, fileName);

			//エラー処理1. 存在チェック
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// 処理内容1-2.　文字列を分割、格納
				String[] items = line.split(",");

				//エラー処理1. 支店定義ファイルのフォーマット確認
				if((items.length != 2) || (!items[0].matches(regex))){
					System.out.println(type + FILE_INVALID_FORMAT);
					return false;
				}

				names.put(items[0], items[1]);
				sales.put(items[0], 0L);
				//このシステムは前日の売上金額を繰り越さないため、読込時の売上金額は「0」円で追加。
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */

	// 処理内容3-1. 支店別集計ファイル書き込み処理
	private static boolean writeFile(String path, String fileName, Map<String, String> names, Map<String, Long> sales) {
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			bw = new BufferedWriter(new FileWriter(file));

			//拡張for文
			for(String key : names.keySet()) {
				bw.write(key + "," +names.get(key) + "," + sales.get(key));
			    bw.newLine();
				System.out.println("書き込み中...");
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
					System.out.println("書き込み終了。ファイル " + fileName + " を閉じます。");
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}