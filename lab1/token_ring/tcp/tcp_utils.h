#ifndef TOKEN_RING_TCP_UTILS_H
#define TOKEN_RING_TCP_UTILS_H

#include "../config.h"

void tcp_init_my_socket(struct sockaddr_in *, uint16_t);

void tcp_setup_successor(struct sockaddr_in*, char*, uint16_t);

#endif //TOKEN_RING_TCP_UTILS_H
