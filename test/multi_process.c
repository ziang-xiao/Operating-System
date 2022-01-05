#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"
#include "strlen.c"

int main() {
    char* child0 = "halt.coff"; 
    char* child1 = "matmult.coff"; 
    char* child2 = "sort.coff";
    char* child3 = "rm.coff"; 
    char* child4 = "mv.coff"; 
    char* child6 = "write1.coff"; 

    int children[5]; 
    int children_status[5]; 

    children[0] = exec(child1, 0, 0); 
    int child_stat = join(children[0], &children_status[0]); 
    if (children_status[0] != 7220) {
        printf("failed matmult\n");
        printf("Expected status 7220 but got %d\n", children_status[0]); 
        exit(0); 
    } 
    printf("Passed Matmult\n"); 
 
    children[1] = exec(child2, 0, 0); 
    child_stat = join(children[1], &children_status[1]); 
    if (children_status[1] != 0) {
        printf("failed sort\n");
        printf("Expected status 0 but got %d\n", children_status[1]); 
    }
    printf("Passed Sort\n"); 

    if (join(children[1], &children_status[1]) != -1) {
        printf("failed join: should not join the child twice\n"); 
    } 
    printf("Passed not joining child twice\n"); 

    char* filename = "lorem.txt";
    char* content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit,  
    sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Urna condimentum 
    mattis pellentesque id nibh tortor id. Nisi vitae suscipit tellus mauris a diam maecenas 
    sed enim. Imperdiet nulla malesuada pellentesque elit eget. Augue ut lectus arcu bibendum 
    at varius vel pharetra vel. Commodo elit at imperdiet dui accumsan sit amet nulla facilisi. 
    Magna etiam tempor orci eu. Quis vel eros donec ac. Accumsan lacus vel facilisis volutpat 
    est velit egestas dui. Dictum varius duis at consectetur lorem donec. Amet consectetur 
    adipiscing elit ut aliquam. Ipsum nunc aliquet bibendum enim facilisis gravida neque 
    convallis a. Rutrum tellus pellentesque eu tincidunt. Ut consequat semper viverra nam 
    libero justo. Cras fermentum odio eu feugiat pretium nibh ipsum. Et tortor consequat id 
    porta nibh venenatis. Elit ullamcorper dignissim cras tincidunt lobortis feugiat. Mattis 
    vulputate enim nulla aliquet porttitor lacus luctus. Commodo viverra maecenas accumsan lacus 
    vel facilisis volutpat. Faucibus scelerisque eleifend donec pretium vulputate sapien nec sagittis 
    aliquam."; 
    int content_len = strlen(content); 

    int fd = creat(filename); 
    if (write(fd, content, content_len) < 0) {
        printf("Failed write:\n"); 
        exit(-1); 
    } 

    char* child4ARGV[] = {child4, "lorem.txt", "test.txt"}; 
    children[2] = exec(child4, 3, child4ARGV); 
    if (children[2] < 0) {
        printf("Failed executing mv.c\n"); 
        exit(-1); 
    }
    
    join(children[2], &children_status[2]); 
    if (children_status[2] != 0) {
        printf("mv.c failed\n"); 
        exit(-1); 
    }
    printf("Passed mv.c\n"); 


    char* child3ARGV[] = {child3, "test.txt"}; 
    children[3] = exec(child3, 2, child3ARGV); 
    if (children[3] < 0) {
        printf("Failed executing rm.c\n"); 
        exit(-1); 
    }

    join(children[3], &children_status[3]); 
    if (children_status[3] != 0) {
        printf("rm.c failed\n"); 
        exit(-1); 
    }
    printf("Passed rm.c\n");

    exit(1); 
}