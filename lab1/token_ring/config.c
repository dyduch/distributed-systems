//
// Created by dyduch on 3/11/19.
//

#include <time.h>
#include "config.h"

int network_size = 0;

int taken_ports[MAX_CLIENTS_AMOUNT] = { 0 };

int take_port(uint16_t port){
    if(taken_ports[port - MIN_PORT_NUMBER] == 0){
        taken_ports[port - MIN_PORT_NUMBER] = 1;
        network_size += 1;
        return 0;
    } else
        return -1;
}

void release_port(uint16_t port){
    taken_ports[port - MIN_PORT_NUMBER] = 0;
    network_size -= 1;
}

uint16_t calculate_receiver(){
    printf("\n");

    for(int i = 0; i < MAX_CLIENTS_AMOUNT; i++){
        printf("%d: %d\n", (i + MIN_PORT_NUMBER), taken_ports[i]);
    }

    printf("\n");


    srand((unsigned int) time(NULL));
    int random = rand()%network_size;
    for(int i = 0; i < MAX_CLIENTS_AMOUNT; i++){
        if(taken_ports[i] == 1 && random == 0)
            return (uint16_t) (i + MIN_PORT_NUMBER);
        else if(taken_ports[i] == 1 && random > 0)
            random -= 1;
    }
    return 0;
}

struct token get_msg_token(char* id, uint16_t sender_port, uint16_t receiver_port, char* msg){

    struct token token;
    token.type = MSG_TOKEN;
    strcpy(token.sender_id, id);
    if(msg == NULL)
        strcpy(token.msg, "First token");
    else
        strcpy(token.msg, msg);
    token.from = sender_port;
    token.to = receiver_port;

    return token;
}

struct token get_init_token(char* id, uint16_t own_port, uint16_t next_port){

    struct token init_token;
    strcpy(init_token.sender_id, id);
    init_token.type = INIT_TOKEN;
    init_token.from = own_port;
    init_token.to = own_port;
    init_token.my_next_client_port = next_port;

    return init_token;
}

struct token get_term_token(char* id, uint16_t own_port, uint16_t next_port){

    struct token term_token;
    strcpy(term_token.sender_id, id);
    term_token.type = TERM_TOKEN;
    term_token.from = own_port;
    term_token.to = own_port;
    term_token.my_next_client_port = next_port;

    return term_token;
}



