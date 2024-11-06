package com.taskapp.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.taskapp.exception.AppException;
import com.taskapp.logic.TaskLogic;
import com.taskapp.logic.UserLogic;
import com.taskapp.model.User;

public class TaskUI {
    private final BufferedReader reader;

    private final UserLogic userLogic;

    private final TaskLogic taskLogic;

    private User loginUser;

    public TaskUI() {
        reader = new BufferedReader(new InputStreamReader(System.in));
        userLogic = new UserLogic();
        taskLogic = new TaskLogic();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * @param reader
     * @param userLogic
     * @param taskLogic
     */
    public TaskUI(BufferedReader reader, UserLogic userLogic, TaskLogic taskLogic) {
        this.reader = reader;
        this.userLogic = userLogic;
        this.taskLogic = taskLogic;
    }

    /**
     * メニューを表示し、ユーザーの入力に基づいてアクションを実行します。
     *
     * @see #inputLogin()
     * @see com.taskapp.logic.TaskLogic#showAll(User)
     * @see #selectSubMenu()
     * @see #inputNewInformation()
     */
    public void displayMenu() {
        System.out.println("タスク管理アプリケーションにようこそ!!");

        //ディスプレイメニューを選択前にinputLogin()へ遷移して、ログインさせる
        inputLogin();

        // メインメニュー
        boolean flg = true;
        while (flg) {
            try {
                System.out.println("以下1~3のメニューから好きな選択肢を選んでください。");
                System.out.println("1. タスク一覧, 2. タスク新規登録, 3. ログアウト");
                System.out.print("選択肢：");
                String selectMenu = reader.readLine();

                System.out.println();

                switch (selectMenu) {
                    case "1":
                        //タスク一覧の表示
                        taskLogic.showAll(loginUser);
                        //タスク一覧表示後に、ステータス更新機能を選択できるサブメニューを追加する
                        selectSubMenu();
                        break;
                    case "2":
                        //ログイン後に表示されるメインメニューから選択できるようにすること
                        inputNewInformation();
                        break;
                    case "3":
                        System.out.println("ログアウトしました。");
                        flg = false;
                        break;
                    default:
                        System.out.println("選択肢が誤っています。1~3の中から選択してください。");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    /**
     * ユーザーからのログイン情報を受け取り、ログイン処理を行います。
     *
     * @see com.taskapp.logic.UserLogic#login(String, String)
     */
    public void inputLogin() {
        boolean flg = true;
        while(flg){
            try{
                System.out.print("メールアドレスを入力してください：");
                String email = reader.readLine();

                System.out.print("パスワードを入力してください：");
                String password = reader.readLine();

                //ログイン処理を呼び出す UserLogicへメアドとパスワードを送る
                loginUser =userLogic.login(email, password);
                System.out.println();
                flg = false;

            }catch(IOException e){
                e.printStackTrace();
            }catch(AppException e){
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    /**
     * ユーザーからの新規タスク情報を受け取り、新規タスクを登録します。
     *
     * @see #isNumeric(String)
     * @see com.taskapp.logic.TaskLogic#save(int, String, int, User)
     */
    public void inputNewInformation() {
        boolean flg = true;
        while(flg){
            try{
                //タスクコード新規登録
                System.out.print("タスクコードを入力してください :");
                String code = reader.readLine();
                //タスクコードは数字か
                //仕様を満たさない場合、「コードは半角の数字で入力してください」と表示し再度タスクコードの入力に戻る
                if(!isNumeric(code)){
                    System.out.println("コードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }
                //タスク名新規登録
                System.out.print("タスク名を入力してください :");
                String name = reader.readLine();
                //タスク名は10文字以内か
                //仕様を満たさない場合、「タスク名は10文字以内で入力してください」と表示し再度タスクコードの入力に戻る
                if(!(name.length()<=10)){
                    System.out.println("タスク名は10文字以内で入力してください");
                    System.out.println();
                    continue;
                }
                //
                System.out.print("担当するユーザーのコードを選択してください :");
                String userCode = reader.readLine();
                //担当するユーザーコードは数字か
                //仕様を満たさない場合、「ユーザーのコードは半角の数字で入力してください」と表示し再度タスクコードの入力に戻る
                if(!isNumeric(userCode)){
                    System.out.println("ユーザーのコードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }

                taskLogic.save(Integer.parseInt(code),name,Integer.parseInt(userCode),loginUser);
            // 担当するユーザーコードが users.csvに登録されていない場合、AppExceptionをスローする
            // スローするときのメッセージは「存在するユーザーコードを入力してください」とする
            // AppExceptionがスローされたらTaskUI側でメッセージを表示し、再度タスクコードの入力に戻る
            }catch (IOException e) {
                e.printStackTrace();
            } catch (AppException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * タスクのステータス変更または削除を選択するサブメニューを表示します。
     *
     * @see #inputChangeInformation()
     * @see #inputDeleteInformation()
     */
    public void selectSubMenu() {
        //サブメニューにて1を選択した場合、以下ステータス更新機能を実行し、2を選択した場合メインメニューの選択に戻る
        boolean flg =true;
        while(flg){
            try{
                System.out.println("以下1~2から好きな選択肢を選んでください。");
                System.out.println("1. タスクのステータス変更, 2. メインメニューに戻る");
                System.out.print("選択肢:");
                String selectMenu = reader.readLine();
                System.out.println();

                switch(selectMenu){
                    
                    case "1":
                        //1を押すとステータスの更新を行う
                        inputChangeInformation();
                        break;
                    case "2":
                        //2を押すとメインメニューに戻る=flgをfalseにする
                        System.out.println("メインメニューに戻ります");
                        flg = false;
                        break;
                    case "3":
                        break;
                    default:
                        System.out.println("選択肢が誤っています。1~2の中から選択してください。");
                        break;
                    }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * ユーザーからのタスクステータス変更情報を受け取り、タスクのステータスを変更します。
     *
     * @see #isNumeric(String)
     * @see com.taskapp.logic.TaskLogic#changeStatus(int, int, User)
     */
    public void inputChangeInformation() {
        //ステータスを変更するタスクコード、変更後のステータスを入力させること
        boolean flg = true;
        while(flg){
            try{
                //ステータスを変更するタスクコード、変更後のステータスを入力させること
                System.out.print("ステータスを変更するタスクコードを入力してください：");
                String taskCode = reader.readLine();
                //半角の数字以外だった場合
                if(!isNumeric(taskCode)){
                    System.out.println("コードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }
                
                System.out.println("どのステータスに変更するか選択してください。");
                System.out.println("1. 着手中, 2. 完了");
                System.out.print("選択肢：");
                String status = reader.readLine();
                //半角数字意外だった場合
                if(!isNumeric(status)){
                    System.out.println("ステータスは半角の数字で入力してください");
                    System.out.println();
                    continue;
                //数字でも1or2出なかった場合
                }else if(!(status.equals("1")||status.equals("2"))){
                    System.out.println("ステータスは1・2の中から選択してください");
                    System.out.println();
                    continue;
                }
                taskLogic.changeStatus(Integer.parseInt(taskCode), Integer.parseInt(status), loginUser);
                flg = false;
                
            }catch(IOException e){
                e.printStackTrace();
            }catch(AppException e){
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }


    /**
     * ユーザーからのタスク削除情報を受け取り、タスクを削除します。
     *
     * @see #isNumeric(String)
     * @see com.taskapp.logic.TaskLogic#delete(int)
     */
    // public void inputDeleteInformation() {
    //     try{
            
    //     }catch(IOException e){
    //         e.printStackTrace();
    //     }catch(AppException e){
    //         System.out.println(e.getMessage());
    //     }
            
    // }


    /**
     * 指定された文字列が数値であるかどうかを判定します。
     * 負の数は判定対象外とする。
     *
     * @param inputText 判定する文字列
     * @return 数値であればtrue、そうでなければfalse
     */
    public boolean isNumeric(String inputText) {
        //allMatchですべての要素が指定条件を満たしているか判断する
        return inputText.chars().allMatch(c -> Character.isDigit((char)c));
    }
}