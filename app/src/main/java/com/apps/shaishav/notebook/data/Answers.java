package com.apps.shaishav.notebook.data;

import java.sql.Date;

/**
 * Created by Shaishav on 22-05-2015.
 */
public class Answers {

    private long id;
    private String question;
    private boolean favorited;
    private String answer;
    private String author;
    private String category;
    private String link;
    private String objectId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestion(){
        return question;
    }

    public void setQuestion(String question){
        this.question=question;
    }

    public void setFavorited(boolean favorited){
        this.favorited=favorited;
    }
    public boolean getFavorited(){
        return favorited;
    }

    public void setAnswer(String answer){
        this.answer=answer;
    }
    public String getAnswer(){
        return answer;
    }
    public String getAuthor(){
        return author;
    }
    public void setAuthor(String author){
        this.author=author;
    }

    public void setCategory(String category){
        this.category=category;
    }

    public String getCategory(){
        return category;
    }

    public String getLink(){
        return link;
    }
    public void setLink(String link){
        this.link=link;
    }

    public void setObjectId(String objectId){
        this.objectId = objectId;
    }

    public String getObjectId(){
        return objectId;
    }



    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return question;
    }
}

