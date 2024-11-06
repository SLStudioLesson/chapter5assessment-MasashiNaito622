package com.taskapp.logic;

import java.time.LocalDate;
import java.util.List;

import com.taskapp.dataaccess.LogDataAccess;
import com.taskapp.dataaccess.TaskDataAccess;
import com.taskapp.dataaccess.UserDataAccess;
import com.taskapp.exception.AppException;
import com.taskapp.model.Log;
import com.taskapp.model.Task;
import com.taskapp.model.User;

public class TaskLogic {
    private final TaskDataAccess taskDataAccess;
    private final LogDataAccess logDataAccess;
    private final UserDataAccess userDataAccess;


    public TaskLogic() {
        taskDataAccess = new TaskDataAccess();
        logDataAccess = new LogDataAccess();
        userDataAccess = new UserDataAccess();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * @param taskDataAccess
     * @param logDataAccess
     * @param userDataAccess
     */
    public TaskLogic(TaskDataAccess taskDataAccess, LogDataAccess logDataAccess, UserDataAccess userDataAccess) {
        this.taskDataAccess = taskDataAccess;
        this.logDataAccess = logDataAccess;
        this.userDataAccess = userDataAccess;
    }

    /**
     * 全てのタスクを表示します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findAll()
     * @param loginUser ログインユーザー
     */
    //ユーザー情報を一覧表示
    public void showAll(User loginUser) {
        List<Task>tasks = taskDataAccess.findAll();

        for(Task task : tasks){
            //statusの値を設定 なしで未着手(0)
            String status = "未着手";
            //Task.javaのstatusを見に行き1だったら着手中とする
            if(task.getStatus()==1){
                status = "着手中";
            //Task.javaのstatusを見に行き2だったら完了とする
            }else if(task.getStatus()==2){
                status = "完了";
            }
            //タスクを担当するユーザーの名前を表示する
            String RepUser;
            // 担当者が今ログインしてるユーザーなら、「あなたが担当しています」と表示する
            //Rep_User_Codeだけでは全部取ってくるので、追加でcodeを入力し、一意に決定した物と比較する
            if(task.getRepUser().getCode() ==loginUser.getCode()){
                RepUser = "あなたが担当しています";
            }else{
                //user.javaの持つデータ全てを持つtask.getRepUser()の中のNameの部分を抽出
                RepUser = task.getRepUser().getName() + "が担当しています";
            }
            System.out.println(task.getCode() +". タスク名 : "+task.getName() +", 担当者名 : "+RepUser +", ステータス : "+status);

        }
    }

    /**
     * 新しいタスクを保存します。
     *
     * @see com.taskapp.dataaccess.UserDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#save(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param name タスク名
     * @param repUserCode 担当ユーザーコード
     * @param loginUser ログインユーザー
     * @throws AppException ユーザーコードが存在しない場合にスローされます
     */
    public void save(int code, String name, int repUserCode,User loginUser) throws AppException {
        //TaskUIで入力を受けたrepUserCode(TaskUIではuserCode変数の中身)を受け取り、中身が空か判断
        User user = userDataAccess.findByCode(repUserCode);
        if(user == null){
            throw new AppException("存在するユーザーコードを入力してください\n");
            
        }
        //新規追加の為、statusは0
        Task task = new Task(code, name, 0, user);
        taskDataAccess.save(task);
        //現時刻をとるので、LocalDate.now()でとってくる
        Log log = new Log(code, loginUser.getCode(), 0, LocalDate.now());
        logDataAccess.save(log);

        System.out.println(task.getName() + "の登録が完了しました。\n");

    }

    /**
     * タスクのステータスを変更します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#update(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param status 新しいステータス
     * @param loginUser ログインユーザー
     * @throws AppException タスクコードが存在しない、またはステータスが前のステータスより1つ先でない場合にスローされます
     */
    public void changeStatus(int code, int status,
                            User loginUser) throws AppException {
        Task task = taskDataAccess.findByCode(code);
        //入力されたタスクコードが tasks.csvに存在しない場合
        // スローするときのメッセージは「存在するタスクコードを入力してください」とする
        if(task==null){
            throw new AppException("存在するタスクコードを入力してください");
        }
        //入力データが前のステータスの入力だった場合
        //スローするときのメッセージは「ステータスは、前のステータスより1つ先のもののみを選択してください」とする
        if(!((task.getStatus() == 0 && status == 1||task.getStatus() ==1 && status == 2))){
            throw new AppException("ステータスは、前のステータスより1つ先のもののみを選択してください");
        }

        //tasks.csvの該当タスクのステータスを変更後のステータスに更新する
        task.setStatus(status);
        taskDataAccess.update(task);

        //logs.csvにデータを1件作成する 変更後のステータス入力
        // Statusは変更後のステータス
        // Change_User_Codeは今ログインしてるユーザーコード
        // Change_Dateは今日の日付
        Log log = new Log(code, loginUser.getCode(), status, LocalDate.now());
        logDataAccess.save(log);

        //ステータス変更完了通知
        System.out.println("ステータスの変更が完了しました。");
    }

    /**
     * タスクを削除します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#delete(int)
     * @see com.taskapp.dataaccess.LogDataAccess#deleteByTaskCode(int)
     * @param code タスクコード
     * @throws AppException タスクコードが存在しない、またはタスクのステータスが完了でない場合にスローされます
     */
    // public void delete(int code) throws AppException {
    // }
}