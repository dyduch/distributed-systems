#include <time.h>
#include "config.h"


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



