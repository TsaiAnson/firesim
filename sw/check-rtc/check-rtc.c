#include <stdlib.h>
#include <stdio.h>
#include "util.h"
#include "mmio.h"

#define MTIME_ADDR 0x0200bff8L

#define ARRAY_SIZE 256
int a[ARRAY_SIZE * ARRAY_SIZE];
int b[ARRAY_SIZE * ARRAY_SIZE];
int c[ARRAY_SIZE * ARRAY_SIZE];


int main(void)
{
  	printf("Starting matmul\n");
    for (int i = 0; i < ARRAY_SIZE; i++) {
        for (int j = 0; j < ARRAY_SIZE; j++) {
            for (int k = 0; k < ARRAY_SIZE; k++) {
                 c[i * ARRAY_SIZE + j] += a[i * ARRAY_SIZE + k] * b[k * ARRAY_SIZE + j];
            }
        }
    }
	printf("%ld cycles\n", rdcycle());

	return 0;
}
