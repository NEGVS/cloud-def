#include <stdio.h>
#include <stdlib.h>
// For malloc, free// 1. Node Definition
typedef struct TreeNode {
int data;
struct TreeNode *left;
struct TreeNode *right;
} TreeNode;

// 2. Node CreationTreeNode* createNode(int data) {// Allocate memory for the new node
    TreeNode* newNode = (TreeNode*)malloc(sizeof(TreeNode));// Check if memory allocation was successfulif (newNode == NULL) {fprintf(stderr, "Error: Memory allocation failed!\n");exit(EXIT_FAILURE); // Exit if memory cannot be allocated
    }
    newNode->data = data;
    newNode->left = NULL;
    newNode->right = NULL;return newNode;
}

// 3. Insertion (Binary Search Tree Property)// Returns the root of the modified subtreeTreeNode* insertNode(TreeNode* root, int data) {// Base case: If the tree (or subtree) is empty, create a new node and return itif (root == NULL) {return createNode(data);
    }
// Recursive step: Insert in the correct subtreeif (data < root->data) {
        root->left = insertNode(root->left, data); // Insert in left subtree
    } else if (data > root->data) {
        root->right = insertNode(root->right, data); // Insert in right subtree
    }// If data == root->data, do nothing (or handle duplicates as needed)return root; // Return the potentially modified root pointer
}

// 4. Traversals// In-order Traversal (Left - Root - Right) -> Prints nodes in ascending order for BSTvoid inorderTraversal(TreeNode* root) {if (root != NULL) {
        inorderTraversal(root->left);printf("%d ", root->data);
        inorderTraversal(root->right);
    }
}

// Pre-order Traversal (Root - Left - Right)void preorderTraversal(TreeNode* root) {if (root != NULL) {printf("%d ", root->data);preorderTraversal(root->left);
        preorderTraversal(root->right);
    }
}

// Post-order Traversal (Left - Right - Root)
void postorderTraversal(TreeNode* root) {if (root != NULL) {
        postorderTraversal(root->left);
        postorderTraversal(root->right);printf("%d ", root->data);
    }
}

// (Level-order traversal requires a queue and is more complex, omitted for brevity here,// but can be added if needed)// 5. Search (Binary Search Tree Property)// Returns the node containing the data, or NULL if not foundTreeNode* searchNode(TreeNode* root, int data) {// Base cases: root is null or data is present at root
    if (root == NULL || root->data == data) {return root;
    }
// Data is greater than root's data -> search in right subtreeif (data > root->data) {return searchNode(root->right, data);
    }
// Data is smaller than root's data -> search in left subtreereturn searchNode(root->left, data);
}

// 6. Finding Minimum and Maximum (BST Property)// Finds the node with the minimum value in a non-empty BST subtreeTreeNode* findMin(TreeNode* node) {if (node == NULL) {return NULL;
    }// The minimum value is the leftmost nodewhile (node->left != NULL) {
        node = node->left;
    }return node;
}

// Finds the node with the maximum value in a non-empty BST subtreeTreeNode* findMax(TreeNode* node) {if (node == NULL) {return NULL;
    }// The maximum value is the rightmost nodewhile (node->right != NULL) {
        node = node->right;
    }return node;
}

// 7. Deletion (Binary Search Tree Property)// Returns the root of the modified subtreeTreeNode* deleteNode(TreeNode* root, int data) {// Base case: Tree is empty
    if (root == NULL) {return root;
    }
// Find the node to be deleted
    if (data < root->data) {
        root->left = deleteNode(root->left, data); // Recur down the left subtree
    } else if (data > root->data) {
        root->right = deleteNode(root->right, data); // Recur down the right subtree
    } else {// Node with the data to be deleted found// Case 1: Node with only one child or no childif (root->left == NULL) {
            TreeNode *temp = root->right;free(root); // Free the memory of the node being deletedreturn temp; // Return the right child (or NULL if no children)
        } else if (root->right == NULL) {
            TreeNode *temp = root->left;free(root); // Free the memoryreturn temp; // Return the left child
        }
// Case 2: Node with two children// Get the inorder successor (smallest node in the right subtree)
        TreeNode* temp = findMin(root->right);
// Copy the inorder successor's content to this node
        root->data = temp->data;
// Delete the inorder successor from the right subtree
        root->right = deleteNode(root->right, temp->data);
    }return root; // Return the potentially modified root pointer
}

// 8. Calculating Height// Helper function to find the maximum of two integersint max(int a, int b) {return (a > b) ? a : b;
}

// Calculates the height of the tree (number of edges on the longest path from root to leaf)// Height of an empty tree is -1, height of a tree with one node is 0.int height(TreeNode* root) {if (root == NULL) {return -1; // Height of an empty tree
    } else {// Compute the height of each subtree
        int leftHeight = height(root->left);int rightHeight = height(root->right);
// Use the larger one and add 1 for the current node's levelreturn 1 + max(leftHeight, rightHeight);
    }
}

// 9. Deleting the Entire Tree// Frees all memory allocated for the tree using post-order traversal logic// Returns NULL to indicate the tree is now emptyTreeNode* deleteTree(TreeNode* root) {if (root != NULL) {
        deleteTree(root->left);  // Delete left subtree
        deleteTree(root->right); // Delete right subtree// printf("Deleting node: %d\n", root->data); // Optional: See deletion orderfree(root);             // Delete current node
    }return NULL; // Return NULL after freeing
}

// --- Main Function for Demonstration ---int main() {
    TreeNode* root = NULL; // Start with an empty tree// --- Insertion ---printf("Inserting nodes...\n");root = insertNode(root, 50);
    root = insertNode(root, 30);
    root = insertNode(root, 20);
    root = insertNode(root, 40);
    root = insertNode(root, 70);
    root = insertNode(root, 60);
    root = insertNode(root, 80);// --- Traversals ---printf("\nIn-order traversal: ");
    inorderTraversal(root); // Expected: 20 30 40 50 60 70 80printf("\nPre-order traversal: ");
    preorderTraversal(root); // Expected: 50 30 20 40 70 60 80printf("\nPost-order traversal: ");
    postorderTraversal(root); // Expected: 20 40 30 60 80 70 50printf("\n");
// --- Search ---printf("\nSearching for node 40...\n");
    TreeNode* foundNode = searchNode(root, 40);if (foundNode != NULL) {printf("Node found! Data: %d\n", foundNode->data);
    } else {printf("Node not found.\n");
    }
printf("Searching for node 90...\n");
    foundNode = searchNode(root, 90);if (foundNode != NULL) {printf("Node found! Data: %d\n", foundNode->data);
    } else {printf("Node not found.\n");
    }
// --- Min/Max ---printf("\nFinding Min and Max...\n");
    TreeNode* minNode = findMin(root);
    TreeNode* maxNode = findMax(root);if (minNode) printf("Minimum value: %d\n", minNode->data); // Expected: 20if (maxNode) printf("Maximum value: %d\n", maxNode->data); // Expected: 80// --- Height ---printf("\nCalculating height...\n");int treeHeight = height(root);printf("Height of the tree: %d\n", treeHeight); // Expected: 2 (if height is edges)// --- Deletion ---printf("\nDeleting node 20 (leaf node)...\n");
    root = deleteNode(root, 20);printf("In-order after deleting 20: ");
    inorderTraversal(root); // Expected: 30 40 50 60 70 80printf("\n");
printf("\nDeleting node 30 (node with one child)...\n");
    root = deleteNode(root, 30);printf("In-order after deleting 30: ");
    inorderTraversal(root); // Expected: 40 50 60 70 80printf("\n");
printf("\nDeleting node 50 (node with two children)...\n");
    root = deleteNode(root, 50);printf("In-order after deleting 50: ");
    inorderTraversal(root); // Expected: 40 60 70 80 (in-order successor 60 replaces 50)printf("\n");printf("Current root data: %d\n", root ? root->data : -1); // Should be 60 (the new root after deletion)// --- Delete Entire Tree ---printf("\nDeleting the entire tree...\n");
    root = deleteTree(root);if (root == NULL) {printf("Tree successfully deleted.\n");
    } else {printf("Error: Tree deletion failed somehow.\n");
    }
return 0; // Indicate successful execution
}