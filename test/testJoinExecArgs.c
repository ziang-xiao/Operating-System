#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main() {

    char* prog = "exit1.coff";
    int pid;
    int val;

    /*************************************/
    printf("Test exec: negative argc:\n");
    pid = exec(prog, -1, 0);
    if( pid != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

    /*************************************/
    printf("Test exec: executable with no .coff extension \n");
    pid = exec("exitexitexit.cof", 0, 0);
    if( pid != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

    /*************************************/
    printf("Test exec: invalid executable \n");
    prog = NULL;
    pid = exec(prog, 0, 0);
    if( pid != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

    pid = exec((char *) 0xBADFFF, 0, 0);
    if( pid != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

     /*************************************/
    printf("Test exec: argc>0, invalid argv \n");
    prog = "exit1.coff";

    pid = exec(prog, 1, NULL);
    if( pid != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

    /*************************************/
    printf("Test exec: argc=0, invalid argv \n");
    prog = "exit1.coff";
    pid = exec(prog, 0, NULL);
    if( pid == -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");



    /*************************************/
    printf("Test join: invalid pid\n");
    int status;

    val = join(-1, &status); 
    if( val != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

    val = join(100, &status); 
    if( val != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");



    /*************************************/
    printf("Test join: join a process twice\n");
    prog = "exit1.coff";
    pid = exec(prog, 0, 0);
    if( pid == -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    //first time joining: sucess
    val = join(pid, &status);
    if( status != 123 ) {
        printf("---FAILED\n");
        exit(1);
    }
    if( val != 1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");

    //2nd time joining: no longer a child
    val = join(pid, &status);
    if( val != -1 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED\n");



    /*************************************/
    printf("Test join: child exited with unhandled exception\n");
    prog = "except1.coff"; //from testing section in writeup
    pid = exec (prog, 0, 0);
    val = join(pid, &status); //return 0
    if( val != 0 ) {
        printf("---FAILED\n");
        exit(1);
    }
    printf("---PASSED:child exited with unhandled exception \n");

    printf("PASSED ALL TESTS IN testJoinExecArgs.c\n");
}