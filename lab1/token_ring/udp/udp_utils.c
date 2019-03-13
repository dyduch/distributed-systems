//
// Created by dyduch on 3/12/19.
//

#include "udp_utils.h"

void init_my_socket(struct sockaddr_in* sockaddr_in, uint16_t port){
    sockaddr_in->sin_family = AF_INET;
    sockaddr_in->sin_addr.s_addr = inet_addr(LOCALHOST);
    sockaddr_in->sin_port = htons(port);
}

void setup_successor_address(struct sockaddr_in* sockaddr_in, char* ip, uint16_t port){
    sockaddr_in->sin_family = AF_INET;
    sockaddr_in->sin_addr.s_addr = inet_addr(ip);
    sockaddr_in->sin_port = htons(port);
}
