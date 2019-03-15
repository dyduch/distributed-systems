#include "tcp_utils.h"

void tcp_init_my_socket(struct sockaddr_in* sockaddr_in, uint16_t port){
    sockaddr_in->sin_family = AF_INET;
    sockaddr_in->sin_addr.s_addr = inet_addr(LOCALHOST);
    sockaddr_in->sin_port = htons(port);
}

void tcp_setup_successor(struct sockaddr_in* sockaddr_in, char* ip, uint16_t port){
    sockaddr_in->sin_family = AF_INET;
    sockaddr_in->sin_addr.s_addr = inet_addr(ip);
    sockaddr_in->sin_port = htons(port);
}
