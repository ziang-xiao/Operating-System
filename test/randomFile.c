#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"
#include "strlen.c"


int
do_creat (char *fname) {
    int fd;

    printf ("creating %s...\n", fname);
    fd = creat (fname);
    if (fd >= 0) {
	printf ("...passed (fd = %d)\n", fd);
    } else {
	printf ("...failed create(%d)\n", fd);
	exit (-1001);
    }
    return fd;
}

int
do_open (char *fname) {
    int fd;

    printf ("opening %s...\n", fname);
    fd = open (fname);
    if (fd >= 0) {
	printf ("...passed (fd = %d)\n", fd);
    } else {
	printf ("...failed open(%d)\n", fd);
	exit (-1000);
    }
    return fd;
}

void
do_close (int fd) {
    int r;

    printf ("closing %d...\n", fd);
    r = close(fd);
    if (r < 0) {
	printf ("...failed close(r = %d)\n", r);
	exit (-1003);
    }
}

int
do_unlink (char *fname) {
    int fd;

    printf ("opening %s...\n", fname);
    fd = unlink (fname);
    if (fd >= 0) {
	printf ("...passed unlinking (fd = %d)\n", fd);
    } else {
	printf ("...failed unlink(%d)\n", fd);
	exit (-1002);
    }
    return fd;
}

int main() {

    int fd[16]; 

    char *filename; 

    filename = "test1.txt"; 
    printf ("opening an uncreated file%s...\n", filename);
    fd[0] = open("test1.txt"); 
    if (fd[0] >= 0) {
        printf("....fail: opened a file that was not created\n"); 
        exit(-5); 
    }
    else printf("...passed not opening an uncreated file...\n"); 
/********************************************************************************************/
    filename = "test2.txt"; 
    do_creat(filename); 
    for (int i = 0; i < 16; i++) {
        fd[i] = open(filename); 
    }
    if (fd[13] != -1 || fd[14] != -1 || fd[15] != -1) {
        printf("Went beyond the maximum of 16 OpenFiles\n"); 
        exit(10); 
    }
    printf("passed the max count of OpenFiles\n"); 
/********************************************************************************************/
    do_unlink(filename); 
    printf ("opening an unlinked file%s...\n", filename);
    fd[0] = open("test1.txt"); 
    if (fd[0] >= 0) {
        printf("....fail: opened a file that was unlinked\n"); 
        exit(-5); 
    }
    else printf("...passed not opening a file that was unlinked...\n"); 
/********************************************************************************************/
    if (creat(filename) != -1) {
        printf("unlink should not close the files in the fileTable\n"); 
        exit(0); 
    } 
/********************************************************************************************/
    for (int i = 2; i < 16; i++) do_close(i); 
    fd[0] = do_creat(filename);
    do_close(fd[0]); 
    for (int i = 0; i < 14; i++) {
        if(open(filename) == -1) {
            printf("failed to open 14 files\n");
            exit(45);
        } 
    }
    printf("passed opening 14 files\n"); 
/********************************************************************************************/

}

