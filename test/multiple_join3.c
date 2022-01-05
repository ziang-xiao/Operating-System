#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

/*
This test will have the main process create a bunch of child processes and call join() on all of them.
The goal is to ensure that we actually terminate.
*/
int main() {
    char *child_prog = "write13.coff"; // something small that actually calls exit()
    int children[100];
    int children_statuses[100];
    int i;
    for (i = 0; i < 100; i++) { // 100 children
        children[i] = exec(child_prog, 0, 0);
        join(children[i], &children_statuses[i]);
        if (children[i] <= 0) {
//            for (int j = 0; j < i; j++) {
//                join(children[j], &children_statuses[j]);
//            }
            printf("exec #%d failed\n", i);
            exit(1);
        }
    }
//    for (i = 0; i < 5; i++) {
//        join(children[i], &children_statuses[i]);
//    }
    for (i = 0; i < 5; i++) {
        if (children_statuses[i] != 123) { // we expect it to be 123 because that's what we call exit() with
            printf("child %d exited with abnormal status %d\n", i, children_statuses[i]);
            exit(1);
        }
    }
    printf("yay!\n");
    return 0;
}
