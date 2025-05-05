package com.github.hanlyjiang.testmode;

public class Test {


    List<Integer> path = new ArrayList<>();

    @Test
    public void test() {
        Node root = new Node("A");
        root.left = new Node("B");
        root.right = new Node("C");
        root.right.left = new Node("F");
        root.left.left = new Node("D");
        root.left.right = new Node("E");
        root.left.left.left = new Node("G");
        travel(root,root.left.right);
    }

    public Node travel(Node root, Node node) {
        if (root == null) {
            return null;
        }
        if (root == node) {
            return root;
        }
        Node left = travel(root.left, node);
        Node right = travel(root.right, node);

        if (left != null) {
            System.out.println("->" + left.value);
            return left;
        }
        System.out.println("->" + (right!=null?right.value:null));
        return right;
    }
}
