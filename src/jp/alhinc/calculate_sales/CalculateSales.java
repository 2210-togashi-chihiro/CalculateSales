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


		// ※ここから(売り上げファイル)集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();

		List<File> rcdFiles = new ArrayList<>();
		// この辺も2-1にあり。(変数名に困ることはなさそう)

		//　2-1　穴埋め1つ目(ファイルパス)
		for(int i = 0; i < files.length ; i++) {
			String fileName = files[i].getName() ;
			//	Stringであることのヒントはない。何が取れるか自分で調べるか。
			//	おそらく初の命名ポイント。変数名が命名規則に沿ってるか確認

			if(files[i].isFile() && fileName.matches("^[0-9]{8}.rcd$")) {
				//2-1穴埋め2つ目（ファイル名）　と　2-1穴埋め2つ目（正規記表現構文）
				rcdFiles.add(files[i]);
				// この辺も2-1にあり。ここがTrueで落ちてくる場所だっていうことはくっきり理解してほしい。
			}
		}

		//処理内容2-2 売上ファイルの読み込み処理　※読み込みなので書き込みより前、2-1のfor文より後
		for(int i = 0; i < rcdFiles.size(); i++) {
			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(rcdFiles.get(i)));
				ArrayList<String> fileContents = new ArrayList<>();

				String line;
				// 一行ずつ読み込む
				while((line = br.readLine()) != null) {
					fileContents.add(line);
				}
				//ファイル名を取得
				String FileName = rcdFiles.get(i).getName();
				//支店名を取得
				String branchCode = fileContents.get(0);

				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//※詳細は後述で説明(処理内容2－2　型の変換)
				long fileSale = Long.parseLong(fileContents.get(1));
				//読み込んだ売上⾦額を加算します。
				//※詳細は後述で説明
				Long saleAmount = branchSales.get(branchCode) + fileSale;

				//加算した売上⾦額をMapに追加します。
				// 処理内容2-2には明記なし。putは自分で探してくる感じかな？　catchとfinallyも自分で持ってくる（参考記載あり）
				//finallyまで書いて、処理内容2-2が終わる。
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
			return;
		}


		// 支店別集計ファイル書き込み処理　※writeFileメソッドを呼び出し返却
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
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//文字列を分割
				String[] items = line.split(",");
				System.out.println(line);
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1) 読み込みと書き込み似てるなとかでたどり着けるか
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			bw = new BufferedWriter(new FileWriter(file));
//		try {
//			File file = new File(path, fileName);
//			FileWriter fw = new FileWriter(file);
//			br = new BufferedWriter(fw);               支店定義ファイル読み込み処理そのまま持ってくるとこの書き方もありそう

			String line;
			//拡張for文で回す。(3-1 Mapから全てのKeyを取得する方法 )
			for(String key : branchNames.keySet()) {
				//◆!!!!ここから続き　記載してね◆
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
