/*
 * exec1.c
 *
 * Simple program for testing exec.  It does not pass any arguments to
 * the child.
 */
#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int
main (int argc, char *argv[])
{
    char *prog = "write2.coff";
    int pid;

    write(1, "trying to call exec\n", 20);
    pid = exec(prog, 0, 0);
    if (pid < 0) {
	    exit (-1);
    }


    int child_stat;
    int ret = join(pid, &child_stat);
    printf("child pid is: %d\n",pid );


    printf ("ret from join (r = %d)\n", child_stat);

    exit (0);
}

