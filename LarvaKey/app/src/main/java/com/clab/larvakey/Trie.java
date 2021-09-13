package com.clab.larvakey;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Trie {
    private CustomVariables mCustomVariables = new CustomVariables();
    Node mRoot;

    public Trie(Context context) {
        this.mRoot = new Node();
        this.mRoot.val = ' ';
        this.mRoot.count = 0;

        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open("word_freq_data.txt", AssetManager.ACCESS_BUFFER);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                String[] pair = line.split(",");
                String s = pair[0];
                insert(s);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Node {
        Node[] child = new Node[mCustomVariables.ALPHABET_SIZE];
        char val;
        int count;
    }

    private int charToInt(char c) {
        return c - 'a';
    }

    public void insert(String str) {
        Node current = this.mRoot;
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);      // 전체 문자열의 일부 단어 추출
            int num = this.charToInt(c); // 추출한 단어를 숫자로 변환

            if (current.child[num] == null) {     // 기존에 null이면 연결 문자열로 처음 추가되는 것
                current.child[num] = new Node();
                current.child[num].val = c;
            }
            current.child[num].count++;

            current = current.child[num];       // 자식 노드로 넘어감
        }
    }

    public int[] find(String str) {
        Node current = this.mRoot;
        int[] ret = new int[mCustomVariables.ALPHABET_SIZE];
        str = str.toLowerCase();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int num = this.charToInt(c);

            if (current.child[num] == null) {
                return ret;
            }
            current = current.child[num];
        }

        for (int i = 0; i < mCustomVariables.ALPHABET_SIZE; i++) {
            if (current.child[i] != null) {
                ret[i] = current.child[i].count;
            }
        }
        return ret;
    }
}
