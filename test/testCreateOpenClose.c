
#include "stdio.h"
#include "stdlib.h"

int main () {
    char *fname;
    int fd;
    int status;

    printf("----------TEST CREATE invalid vaName: RETURN -1----------\n");
    fname = NULL;
    fd = creat(fname);
    if (fd == -1) {
	    printf ("PASSED (fd = %d)\n", fd);
    } else {
	    printf ("FAILED (%d)\n", fd);
	    exit (-1001);
    }

    printf("----------TEST OPEN invalid vaName: RETURN -1----------\n");
    fname = NULL;
    fd = open(fname);
    if (fd == -1) {
	    printf ("PASSED (fd = %d)\n", fd);
    } else {
	    printf ("FAILED (%d)\n", fd);
	    exit (-1001);
    }

    printf("----------TEST UNLINK invalid vaName: RETURN -1----------\n");
    fname = NULL;
    status = unlink(fname);
    if (status == -1) {
	    printf ("PASSED\n");
    } else {
	    printf ("FAILED\n");
	    exit (-1001);
    }

    printf("----------TEST CREATE new file: RETURN valid fd----------\n");
    fname = "aNewFile.txt";
    fd = creat(fname);
    if (fd != -1) {
	    printf ("PASSED (fd = %d)\n", fd);
    } else {
	    printf ("FAILED (%d)\n", fd);
	    exit (-1001);
    }

    printf("----------TEST UNLINK opened file: RETURN 0----------\n");
    fname = "aNewFile.txt";
    open(fname);
    status = unlink(fname);
    if (status == 0) {
	    printf ("PASSED removing %s, CHECK it's not in the folder anymore\n", fname);
    } else {
	    printf ("FAILED \n");
	    exit (-1001);
    }

    printf("----------TEST UNLINK do not exist file: RETURN -1----------\n");
    fname = "dne.txt";
    status = unlink(fname);
    if (status == -1) {
	    printf ("PASSED \n");
    } else {
	    printf ("FAILED \n");
	    exit (-1001);
    }

    printf("----------TEST CLOSE a opened file: RETURN 0----------\n");
    fname = "newFile.txt";
    fd = creat(fname);
    status = close(fd);
    if (status == 0) {
	    printf ("PASSED closing (fd = %d)\n", fd);
    } else {
	    printf ("FAILED closing (%d)\n", fd);
	    exit (-1001);
    }

    printf("----------TEST UNLINK closed file: RETURN 0----------\n");
    fname = "newFile.txt";
    fd = creat(fname);
    close(fd);
    status = unlink(fname);
    if (status == 0) {
	    printf ("PASSED removing %s\n", fname);
    } else {
	    printf ("FAILED \n");
	    exit (-1001);
    }

    printf("----------TEST OPEN already existed file: RETURN valid fd----------\n");
    fname = "rm.c";
    fd = open(fname);
    if (fd != -1) {
	    printf ("PASSED (fd = %d)\n", fd);
    } else {
	    printf ("FAILED (%d)\n", fd);
	    exit (-1001);
    }

    printf("----------TEST OPEN do not existed file: RETURN -1----------\n");
    fname = "dne.txt";
    fd = open(fname);
    if (fd == -1) {
	    printf ("PASSED (fd = %d)\n", fd);
    } else {
	    printf ("FAILED (%d)\n", fd);
	    exit (-1001);
    }

    printf("----------TEST OPEN already existed file: RETURN valid fd----------\n");
    fname = "alreadyExist.txt";
    int fd1 = open(fname);
    if (fd1 != -1) {
	    printf ("PASSED (fd = %d)\n", fd1);
    } else {
	    printf ("FAILED (%d)\n", fd1);
	    exit (-1001);
    }

    printf("----------TEST OPEN already opened file: different fd from above----------\n");
    fname = "alreadyExist.txt";
    int fd2 = open(fname);
    if (fd2 != -1) {
	    printf ("PASSED (fd = %d) (CHECK THAT fd different from above) \n", fd2);
    } else {
	    printf ("FAILED (%d)\n", fd2);
	    exit (-1001);
    }

    printf("----------TEST CREATE already existed file: different fd from above truncated----------\n");
    fname = "alreadyExist.txt";
    int fd3 = creat(fname);
    if (fd3 != -1) {
	    printf ("PASSED (fd = %d) (CHECK THAT fd different from above, file truncated) \n", fd3);
    } else {
	    printf ("FAILED (%d)\n", fd3);
	    exit (-1001);
    }

    printf("----------TEST CLOSE already closed file----------\n");
    fname = "alreadyExist.txt";
    fd1 = open(fname);
    status = close(fd1);
    if (status == 0) {
	    printf ("PASSED closing file at fd=%d\n", fd1);
    } else {
	    printf ("FAILED\n");
	    exit (-1001);
    }
    status = close(fd1);
    if (status == -1) {
	    printf ("PASSED closing already closed file return %d\n", status);
    } else {
	    printf ("FAILED\n");
	    exit (-1001);
    }
    // fd = open("alreadyExist.txt");
    // if(fd==fd1) {
    //     printf("PASSED fd=%d is reused\n", fd1);
    // } else {
    //     printf("FAILED");
    // }

    printf("----------TEST CLOSE file out of range fd: RETURN -1----------\n");
    status = close(-1);
    if (status == -1) {
	    printf ("PASSED\n");
    } else {
	    printf ("FAILED\n");
	    exit (-1001);
    }
    status = close(16);
    if (status == -1) {
	    printf ("PASSED\n");
    } else {
	    printf ("FAILED\n");
	    exit (-1001);
    }
    status = close(123456);
    if (status == -1) {
	    printf ("PASSED\n");
    } else {
	    printf ("FAILED\n");
	    exit (-1001);
    }

    printf("PASSED ALL TESTS IN testCreateOpenClose!\n");

    return 0;
}