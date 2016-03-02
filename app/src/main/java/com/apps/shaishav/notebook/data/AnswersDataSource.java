package com.apps.shaishav.notebook.data;

/**
 * Created by Shaishav on 22-05-2015.
 */
        import java.util.ArrayList;
        import java.util.List;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;

public class AnswersDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;


    public AnswersDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Answers createComment(String comment,String answer,String author,String category,boolean favorited,String link,String objectId) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_QUESTION, comment);
        values.put(MySQLiteHelper.COLUMN_ANSWER,answer);
        values.put(MySQLiteHelper.COLUMN_AUTHOR,author);
        values.put(MySQLiteHelper.COLUMN_CATEGORY,category);
        values.put(MySQLiteHelper.COLUMN_FAVORITED,favorited);
        values.put(MySQLiteHelper.COLUMN_LINK,link);
        values.put(MySQLiteHelper.COLUMN_OBJECTID,objectId);

        long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, MySQLiteHelper.COLUMN_ID+" desc");
        cursor.moveToFirst();
        Answers newComment = cursorToComment(cursor);
        cursor.close();
        return newComment;
    }

    /*public void updateComment(String comment,boolean completed,long createdat, long id){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
        values.put(MySQLiteHelper.COLUMN_COMPLETED,completed);
        values.put(MySQLiteHelper.COLUMN_CREATEDAT,createdat);
        String where = MySQLiteHelper.COLUMN_ID+"= ?";
        String selectionArgs[] = {String.valueOf(id)};
        database.update(MySQLiteHelper.TABLE_COMMENTS,values,where,selectionArgs);

    }*/

    public void updateFavorited(long id,boolean favorited){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_FAVORITED, favorited);
        String where = MySQLiteHelper.COLUMN_ID+"= ?";
        String selectionArgs[] = {String.valueOf(id)};
        database.update(MySQLiteHelper.TABLE_COMMENTS,values,where,selectionArgs);
    }

    public void deleteComment(Answers tasks) {
        long id = tasks.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Answers> searchComments(String param){
        List<Answers> comments = new ArrayList<Answers>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, MySQLiteHelper.COLUMN_QUESTION+" like '%"+param+"%' OR "+MySQLiteHelper.COLUMN_AUTHOR+" like '%"+param+"%'", null, null, null, MySQLiteHelper.COLUMN_ID+" desc");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Answers comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    public List<Answers> getAllComments() {
        List<Answers> comments = new ArrayList<Answers>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, null, null, null, null, MySQLiteHelper.COLUMN_ID+" desc");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Answers comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    public List<Answers> getAuthorsOrCategories(String type){
        List<Answers> comments = new ArrayList<Answers>();
        Cursor cursor;
        if(type.equals("author")){
            cursor=database.query(true,MySQLiteHelper.TABLE_COMMENTS,null,null,null,MySQLiteHelper.COLUMN_AUTHOR,null,null,null);
        }
        else{
            cursor=database.query(true,MySQLiteHelper.TABLE_COMMENTS,null,null,null,MySQLiteHelper.COLUMN_CATEGORY,null,null,null);

        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Answers comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;


    }

    public List<Answers> getCustom(String type,String param){
        List<Answers> comments = new ArrayList<Answers>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, type+" like '"+param+"'", null, null, null, MySQLiteHelper.COLUMN_ID+" desc");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Answers comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    public List<Answers> getFavoritedAnswers(){
        List<Answers> comments = new ArrayList<Answers>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, MySQLiteHelper.COLUMN_FAVORITED+" = 1", null, null, null, MySQLiteHelper.COLUMN_ID+" desc");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Answers comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;

    }

    public boolean checkForExistingAnswer(String link){
        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, MySQLiteHelper.COLUMN_LINK+" like '"+link+"'", null, null, null, MySQLiteHelper.COLUMN_ID+" desc");

        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public List<Answers> getUnsynced(){
        List<Answers> comments = new ArrayList<Answers>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                null, MySQLiteHelper.COLUMN_OBJECTID+" like 'not set'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Answers comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    public void updateObjectId(String objectId,long id){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_OBJECTID, objectId);
        String where = MySQLiteHelper.COLUMN_ID+"= ?";
        String selectionArgs[] = {String.valueOf(id)};
        database.update(MySQLiteHelper.TABLE_COMMENTS,values,where,selectionArgs);
    }

    private Answers cursorToComment(Cursor cursor) {
        Answers comment = new Answers();
        comment.setId(cursor.getLong(0));
        comment.setQuestion(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_QUESTION)));
        comment.setAnswer(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ANSWER)));
        comment.setAuthor(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_AUTHOR)));
        comment.setCategory(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_CATEGORY)));

        boolean completed = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_FAVORITED))>0;

        comment.setFavorited(completed);
        comment.setLink(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LINK)));
        comment.setObjectId(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_OBJECTID)));

        return comment;
    }
}
