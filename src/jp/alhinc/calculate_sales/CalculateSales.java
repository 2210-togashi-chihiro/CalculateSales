package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// 処理内容2-1. 売上ファイル検索
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			String fileName = files[i].getName() ;
			//「String fileName =」は転記無し。Stringになることは自力でたどり着く。
			//	おそらく初の命名ポイント。変数名が命名規則に沿ってるか確認

			//穴埋め箇所「if(ファイル名.matches(正規表現構⽂)) 」
			if(files[i].isFile() && fileName.matches("^[0-9]{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}

		}
/*	★★★ここから記述してください★★★
		//エラー処理2-1.　売上ファイル名 連番チェック
		for(int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.substring(0, 8));
			int latter = Integer.parseInt(次のファイル名.substring(0, 8));

		      //⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を⽐較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
			}
		}

*/
		//処理内容2-2 売上ファイル読込処理　※読み込みなので書き込みより前、2-1の検索より後
		//支店定義ファイル読込(readFileメソッド)を参考に。
		BufferedReader br = null;

		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				br = new BufferedReader(new FileReader(rcdFiles.get(i)));
				ArrayList<String> fileContents = new ArrayList<>();

				String line;
				// 一行ずつ読み込む　※売上を加算しないといけないため、支店定義のようにそのままマップに落とすわけにはいかない。
				while((line = br.readLine()) != null) {
					fileContents.add(line);
				}

				//支店名を取得(売上集計ファイルの中身＝branchで1行、Saleで1行の計2行を記録)
				String branchCode = fileContents.get(0);

				//型の変換
				long fileSale = Long.parseLong(fileContents.get(1));
				//読み込んだ売上⾦額(fileSale)を加算、Mapに追加
				Long saleAmount = branchSales.get(branchCode) + fileSale;
				branchSales.put(branchCode, saleAmount);

				//catchとfinallyも自分で持ってきて、処理内容2-2が終わる
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


		// 処理内容3-1. 支店別集計ファイル書き込み処理　※writeFileメソッドを呼び出し返却
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
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
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))){
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			bw = new BufferedWriter(new FileWriter(file));
//		try {
//			File file = new File(path, fileName);
//			FileWriter fw = new FileWriter(file);
//			br = new BufferedWriter(fw);               支店定義ファイル読み込み処理そのまま持ってくるとこの書き方もありそう

			//拡張for文
			for(String key : branchNames.keySet()) {
				bw.write(key + "," +branchNames.get(key) + "," + branchSales.get(key));
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