#include "stdio.h"
#include "stdlib.h"

int bigbuf1[1024];
int bigbuf2[1024];
int bigbufnum = 1024;

void testReadArg() {
    int fd;
    char buf[128];
    int val;

    printf("-----TEST: negative count-----\n");
    fd = open("to_read.txt");

    //valid fd, valid buf
    val = read(fd, buf, -1);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //invalid fd, valid buf
    val = read(-1, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(16, buf, -2);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //valid fd, invalid buf
    val = read(fd, (char *) 0xBADFFF, -1);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //invalid fd, invalid buf
    val = read(-10, (char *) 0xBADFFF, -200);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    close(fd);


    printf("-----TEST: nonzero count, invalid buf-----\n");
    fd = open("to_read.txt");
    // valid fd
    val = read(fd, (char *) 0xBADFFF, 100);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    close(fd);

    fd = open("to_read.txt");
    val = read(fd, (char *) 0, (80 * 1024));
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    close(fd);

    fd = open("to_read.txt");
    val = read(fd, (char *) 0, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    close(fd);

    // invalid fd
    val = read(-10, (char *) 0xBADFFF, 100);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(-10, (char *) 0, (80 * 1024));
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(-10, (char *) 0, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    close(fd);

    printf("-----TEST: invalid fd-----\n");
    
    //fd out of range, valid buf, valid count
    val = read(-1, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(16, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(170, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd out of range, invalid buf, valid count
    val = read(-1, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(16, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(170, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd out of range, valid buf, invalid count
    val = read(-1, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(16, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(170, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd not in use, valid buf, valid count
    val = read(10, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(15, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd not in use, invalid buf, valid count
    val = read(10, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(15, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd not in use, valid buf, invalid count
    val = read(10, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = read(15, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    printf("-----TEST: zero count, valid fd-----\n");
    fd = open("to_read.txt");
    
    //invalid buf
    val = read(fd, (char *) 0xBADFFF, 0);
    if(val!=0) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //valid buf
    val = read(fd, buf, 0);
    if(val!=0) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    printf("-----TEST: read from empty file, invalid buf-----\n");
    fd = creat("empty.txt");

    val = read(fd, (char *) 0xBADFFF, 0);
    if(val!=0) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    val = read(fd, (char *) 0xBADFFF, 1024);
    if(val!=0) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    close(fd);

    printf("PASSED ALL TESTS IN READ:ARGUMENTS\n");
}



void testWriteArg() {
    char *buf = "this is just a test\nwoot woot\ntest a just is this\n\n\nyay!";

    int fd;
    int val;

    printf("-----TEST: negative count-----\n");
    fd = creat("aFile.txt");

    //valid fd, valid buf
    val = write(fd, buf, -1);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //invalid fd, valid buf
    val = write(-1, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(16, buf, -2);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //valid fd, invalid buf
    val = write(fd, (char *) 0xBADFFF, -1);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //invalid fd, invalid buf
    val = write(-10, (char *) 0xBADFFF, -200);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    close(fd);


    printf("-----TEST: nonzero count, invalid buf-----\n");
    fd = open("aFile.txt");

    // valid fd
    val = write(fd, (char *) 0xBADFFF, 100);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(fd, (char *) 0, (80 * 1024));
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    // invalid fd
    val = write(-10, (char *) 0xBADFFF, 100);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(-10, (char *) 0, (80 * 1024));
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    close(fd);


    printf("-----TEST: invalid fd-----\n");
    
    //fd out of range, valid buf, valid count
    val = write(-1, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(16, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(170, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd out of range, invalid buf, valid count
    val = write(-1, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(16, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(170, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd out of range, valid buf, invalid count
    val = write(-1, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(16, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(170, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd not in use, valid buf, valid count
    val = write(10, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(15, buf, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd not in use, invalid buf, valid count
    val = write(10, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(15, (char *) 0xBADFFF, 10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //fd not in use, valid buf, invalid count
    val = write(10, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    val = write(15, buf, -10);
    if(val!=-1) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    printf("-----TEST: zero count, valid fd-----\n");
    fd = open("aFile.txt");
    
    //invalid buffer
    val = write(fd, (char *) 0xBADFFF, 0);
    if(val!=0) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");

    //valid buffer
    val = write(fd, buf, 0);
    if(val!=0) {
        printf("FAILED\n");
        exit(1);
    }
    else printf("PASSED\n");
    close(fd);

    printf("PASSED ALL TESTS IN WRITE:ARGUMENTS\n");
}

int main() {
    
    testReadArg();
    testWriteArg();
    printf("PASSED ALL TESTS IN testRead.c\n");

}