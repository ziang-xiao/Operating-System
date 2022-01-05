#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

/*
This test will have the main process create a bunch of child processes and call join() on all of them.
The goal is to ensure that we actually terminate.
*/
int main() {
    char *child_prog = "exit1.coff"; // something small that actually calls exit()
    int children[5];
    int children_statuses[5];
    int i;
    for (i = 0; i < 5; i++) { // 5 children
        children[i] = exec(child_prog, 0, 0);
        if (children[i] <= 0) {
            printf("exec #%d failed\n", children[i]);
            exit(1);
        }
    }
    for (i = 0; i < 5; i++) {
        join(children[i], &children_statuses[i]);
    }
    for (i = 0; i < 5; i++) {
        if (children_statuses[i] != 123) { // we expect it to be 123 because that's what we call exit() with
            printf("child %d exited with abnormal status %d\n", i, children_statuses[i]);
            exit(1);
        }
    }
    printf("yay!\n");
    return 0;
}