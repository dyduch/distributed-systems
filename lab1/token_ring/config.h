#ifndef TOKEN_RING_CONFIG_H
#define TOKEN_RING_CONFIG_H

#include <stdint.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <ctype.h>
#include <stdbool.h>
#include <signal.h>
#include <stdbool.h>

#define LOCALHOST "127.0.0.1"

#define TERM_TOKEN -1
#define INIT_TOKEN 1
#define MSG_TOKEN 0

#define MAX_CLIENTS_AMOUNT 1000
#define MIN_PORT_NUMBER 9000

#define UDP 0
#define TCP 1

#define SLEEP_TIME 2

struct token {
    int type;
    uint16_t to;
    uint16_t from;
    uint16_t my_next_client_port;
    char sender_id[100];
    char msg[500];
};


uint16_t calculate_receiver();

struct token get_msg_token(char*, uint16_t, uint16_t, char*);

struct token get_init_token(char*, uint16_t, uint16_t);

struct token get_term_token(char*, uint16_t, uint16_t);

#endif //TOKEN_RING_CONFIG_H
