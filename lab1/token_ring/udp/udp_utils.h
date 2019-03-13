#ifndef TOKEN_RING_UDP_UTILS_H
#define TOKEN_RING_UDP_UTILS_H

#include "../config.h"

void init_my_socket(struct sockaddr_in*, uint16_t);

void setup_successor_address(struct sockaddr_in*, char*, uint16_t);

#endif //TOKEN_RING_UDP_UTILS_H
