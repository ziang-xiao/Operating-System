#include "syscall.h"

int main (int argc, char *argv[])
{
    for (int i = 0; i < 14; i++) {
	    int r = creat("testcreat1");
	    //printf("fd:%d", r);
	}
    write(3, "testtesttesttesttesttesttesttesttest", 26);
    return 0;
}
