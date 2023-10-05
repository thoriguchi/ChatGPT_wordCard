import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Collections;
import java.util.List;

public class VocabularyApp extends JFrame {
    private JLabel wordLabel;
    private JLabel meaningLabel;
    private JLabel exampleLabel;
    private JButton nextButton;
    private JButton shuffleButton; // シャッフルボタン
    private JButton generateExampleButton;

    private List<WordAndMeaning> wordAndMeaningList; // 単語と意味を保持するリスト
    //private List<String> words; // リストをシャッフルするためにArrayListからListに変更
    //private List<String> meanings; // リストをシャッフルするためにArrayListからListに変更
    private int currentIndex;

    public VocabularyApp() {
        setTitle("単語帳アプリ");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //words = new ArrayList<>();
        //meanings = new ArrayList<>();
        wordAndMeaningList = new ArrayList<>();
        currentIndex = 0;

        // 単語と意味のデータをファイルから読み込む
        readWordMeaningData("word_meaning.txt");

        wordLabel = new JLabel();
        meaningLabel = new JLabel();
        exampleLabel = new JLabel();
        nextButton = new JButton("次の単語");
        generateExampleButton = new JButton("例文生成"); // 例文生成ボタンを追加
        shuffleButton = new JButton("シャッフル"); // シャッフルボタンを追加
        
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex < wordAndMeaningList.size()) {
                    currentIndex++;
                    updateWordDisplay();
                }
            }
        });
        
        shuffleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // リストをシャッフル
                Collections.shuffle(wordAndMeaningList);
                //Collections.shuffle(words);
                //Collections.shuffle(meanings);
                currentIndex = 0;
                updateWordDisplay();
            }
        });

        generateExampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex < wordAndMeaningList.size()) {
                    String word = wordAndMeaningList.get(currentIndex).getWord();
                    String example = fetchExampleFromWebService(word);
                    exampleLabel.setText("<html><body>例文: " + example+"</body></html>");
                }
            }
        });


        setLayout(new GridLayout(6, 1)); // レイアウトに1つのボタンを追加
        add(wordLabel);
        add(meaningLabel);
        add(exampleLabel);
        add(nextButton);
        add(shuffleButton); // シャッフルボタンを追加

        add(generateExampleButton); // 例文生成ボタンを追加

        updateWordDisplay();
    }

    private void readWordMeaningData(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    wordAndMeaningList.add(new WordAndMeaning(parts[0].trim(),parts[1].trim()));
                    //words.add(parts[0].trim());
                    //meanings.add(parts[1].trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fetchExampleFromWebService(String word) {
        String example = null;
        try {
            // 送信先のURLを設定
            //URL url = new URL("http://localhost:8888/post-endpoint");
            URL url = new URL("http://192.168.1.5:8888");

            // HttpURLConnectionを作成
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 送信するバイトデータを作成
            byte[] byteData = word.getBytes();

            // リクエストボディにバイトデータを書き込む
            try (OutputStream os = connection.getOutputStream()) {
                os.write(byteData);
                os.flush();
            }

            // レスポンスを受け取る
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNextLine()) {
                    if(example==null)example=scanner.nextLine();
                    else example+=scanner.nextLine();
                    //System.out.println(scanner.nextLine());
                }
                scanner.close();
            } else {
                System.out.println("POSTリクエストが失敗しました。レスポンスコード: " + responseCode);
            }

            // 接続を閉じる
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrangeSentence(example);
    }
    private String arrangeSentence(String str){
        String arrange=str.substring("English:".length());
        arrange=arrange.replace("Japanese:","<br />");
        return arrange;
    }


    private void updateWordDisplay() {
        if (currentIndex < wordAndMeaningList.size()) {
            WordAndMeaning wordAndMeaning = wordAndMeaningList.get(currentIndex);
            wordLabel.setText("単語: " + wordAndMeaning.getWord());
            meaningLabel.setText("意味: " + wordAndMeaning.getMeaning());
            exampleLabel.setText("<html><body>例文: </body></html>");
        } else {
            wordLabel.setText("単語帳終了");
            meaningLabel.setText("");
            exampleLabel.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VocabularyApp app = new VocabularyApp();
                app.setVisible(true);
            }
        });
    }
}
class WordAndMeaning {
    private String word;
    private String meaning;

    public WordAndMeaning(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }
}
