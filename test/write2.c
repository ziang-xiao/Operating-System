
#include "syscall.h"

int main (int argc, char *argv[])
{
    write(1, "performing invalid op\n", 22);
    char* invalid_ptr = 0x0;
    *invalid_ptr = 1;

    exit(200);

    write(1, "should not reach here", 21);
    return 0;
}
