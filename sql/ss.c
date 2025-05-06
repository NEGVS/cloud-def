#include <stdio.h>
#include <stdlib.h>

// 定义链表节点结构
typedef struct Node {
    int data;           // 数据域
    struct Node* next;  // 指针域，指向下一个节点
} Node;

// 定义链表结构
typedef struct LinkedList {
    Node* head;         // 头指针
} LinkedList;

// 初始化链表
LinkedList* createList() {
    LinkedList* list = (LinkedList*)malloc(sizeof(LinkedList));
    if (list == NULL) {
        printf("Memory allocation failed!\n");
        exit(1);
    }
    list->head = NULL;
    return list;
}

// 在链表头部插入节点
void insertAtHead(LinkedList* list, int data) {
    Node* newNode = (Node*)malloc(sizeof(Node));
    if (newNode == NULL) {
        printf("Memory allocation failed!\n");
        exit(1);
    }
    newNode->data = data;
    newNode->next = list->head;
    list->head = newNode;
}

// 在链表尾部插入节点
void insertAtTail(LinkedList* list, int data) {
    Node* newNode = (Node*)malloc(sizeof(Node));
    if (newNode == NULL) {
        printf("Memory allocation failed!\n");
        exit(1);
    }
    newNode->data = data;
    newNode->next = NULL;

    if (list->head == NULL) {
        list->head = newNode;
        return;
    }

    Node* current = list->head;
    while (current->next != NULL) {
        current = current->next;
    }
    current->next = newNode;
}

// 在指定位置插入节点（从 0 开始计数）
void insertAtPosition(LinkedList* list, int data, int position) {
    if (position < 0) {
        printf("Invalid position!\n");
        return;
    }

    if (position == 0) {
        insertAtHead(list, data);
        return;
    }

    Node* newNode = (Node*)malloc(sizeof(Node));
    if (newNode == NULL) {
        printf("Memory allocation failed!\n");
        exit(1);
    }
    newNode->data = data;

    Node* current = list->head;
    for (int i = 0; i < position - 1 && current != NULL; i++) {
        current = current->next;
    }

    if (current == NULL) {
        printf("Position out of range!\n");
        free(newNode);
        return;
    }

    newNode->next = current->next;
    current->next = newNode;
}

// 删除头部节点
void deleteAtHead(LinkedList* list) {
    if (list->head == NULL) {
        printf("List is empty!\n");
        return;
    }

    Node* temp = list->head;
    list->head = list->head->next;
    free(temp);
}

// 删除尾部节点
void deleteAtTail(LinkedList* list) {
    if (list->head == NULL) {
        printf("List is empty!\n");
        return;
    }

    if (list->head->next == NULL) {
        free(list->head);
        list->head = NULL;
        return;
    }

    Node* current = list->head;
    while (current->next->next != NULL) {
        current = current->next;
    }

    free(current->next);
    current->next = NULL;
}

// 删除指定位置的节点（从 0 开始计数）
void deleteAtPosition(LinkedList* list, int position) {
    if (list->head == NULL) {
        printf("List is empty!\n");
        return;
    }

    if (position < 0) {
        printf("Invalid position!\n");
        return;
    }

    if (position == 0) {
        deleteAtHead(list);
        return;
    }

    Node* current = list->head;
    for (int i = 0; i < position - 1 && current != NULL; i++) {
        current = current->next;
    }

    if (current == NULL || current->next == NULL) {
        printf("Position out of range!\n");
        return;
    }

    Node* temp = current->next;
    current->next = temp->next;
    free(temp);
}

// 查找指定值的节点
Node* search(LinkedList* list, int data) {
    Node* current = list->head;
    while (current != NULL) {
        if (current->data == data) {
            return current;
        }
        current = current->next;
    }
    return NULL;
}

// 遍历链表并打印
void printList(LinkedList* list) {
    Node* current = list->head;
    if (current == NULL) {
        printf("List is empty\n");
        return;
    }

    while (current != NULL) {
        printf("%d -> ", current->data);
        current = current->next;
    }
    printf("NULL\n");
}

// 销毁链表
void destroyList(LinkedList* list) {
    Node* current = list->head;
    while (current != NULL) {
        Node* temp = current;
        current = current->next;
        free(temp);
    }
    free(list);
}

// 主函数测试---------------------------
int main() {
    LinkedList* list = createList();

    // 插入测试
    insertAtHead(list, 10);
    insertAtHead(list, 20);
    insertAtTail(list, 30);
    insertAtPosition(list, 15, 1);
    printf("After insertions: ");
    printList(list);

    // 查找测试
    Node* found = search(list, 15);
    if (found) {
        printf("Found: %d\n", found->data);
    } else {
        printf("Not found\n");
    }

    // 删除测试
    deleteAtHead(list);
    printf("After deleting head: ");
    printList(list);

    deleteAtTail(list);
    printf("After deleting tail: ");
    printList(list);

    deleteAtPosition(list, 1);
    printf("After deleting position 1: ");
    printList(list);

    // 销毁链表
    destroyList(list);
    printf("List destroyed\n");

    return 0;
}