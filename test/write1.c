
#include "syscall.h"

int main (int argc, char *argv[])
{
//    char buf1[30];
//    read(0, buf1, 10);
//    write(1, buf1, 10);

//    write(1, "output test!\n", 12);
//    int f1 = creat("testcreat1");
//    write(f1, "testtesttesttesttesttesttesttest", 31);
//
//    int f2 = creat("testcreat2");

//    char *str = "\nroses are red\nviolets are blue\nI love Nachos\nand so do you\n\n";
    int f1 = open("testcreat1");
    char buf[30];
    int r = read(f1, buf, 23);
    if (r == -1) {
        exit (-1);
    }
    write(1, "read content:\n", 14);
    write(1, buf, 23);
    write(1, "\n",1);

    int f2 = open("testcreat1");
    write(f2, "abcdef", 6);
    write(f2, "higklm", 6);
//    r = write (f2, buf1, 21);
//    if (r = -1) {
//        exit (-1);
//    }

    exit(200);

    write(1, "should not reach here", 21);
    return 0;
}
