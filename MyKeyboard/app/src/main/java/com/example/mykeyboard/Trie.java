package com.example.mykeyboard;

import android.util.SparseArray;

public class Trie {
    static final int ALPHABET_SIZE = 26;
    Node mRoot;

    public Trie() {
        this.mRoot = new Node();
        this.mRoot.val = ' ';
        this.mRoot.count = 0;
    }

    private static class Node {
        Node[] child = new Node[ALPHABET_SIZE];
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

            if(current.child[num] == null){     // 기존에 null이면 연결 문자열로 처음 추가되는 것
                current.child[num] = new Node();
                current.child[num].val = c;
                current.child[num].count++;
            }

            current = current.child[num];       // 자식 노드로 넘어감
        }
    }

    public int[] find(String str) {
        Node current = this.mRoot;
        int[] ret = new int[ALPHABET_SIZE];

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int num = this.charToInt(c);

            if (current.child[num] == null) {
                return ret;
            }
            current = current.child[num];
        }

        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (current.child[i] != null) {
                ret[i] = current.child[i].count;
            }
        }
        return ret;
    }
}
