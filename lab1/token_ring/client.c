//
// Created by dyduch on 3/10/19.
//


#include <unistd.h>
#include <time.h>
#include "config.h"
#include "udp/udp_utils.h"

void init_client(int argc, char **argv);

// client parameters
char *client_id;
uint16_t my_port;
char *next_client_ip;
uint16_t next_client_port;
int has_token_initially;
int protocol_type;

struct sockaddr_in my_addr;
struct sockaddr_in next_addr;

int udp_socket;

int running = 1;

int taken_ports[MAX_CLIENTS_AMOUNT] = {0};
int network_size = 0;


void int_handler(int);


int main(int argc, char **argv) {

    init_client(argc, argv);

    signal(SIGINT, int_handler);

    if (protocol_type == UDP) {

        // my socket initialization
        udp_socket = socket(PF_INET, SOCK_DGRAM, 0);
        init_my_socket(&my_addr, my_port);
        bind(udp_socket, (struct sockaddr *) &my_addr, sizeof(my_addr));

        setup_successor_address(&next_addr, next_client_ip, next_client_port);

        struct token init_token = get_init_token(client_id, my_port, next_client_port);

        sendto(udp_socket, &init_token, sizeof(init_token), 0, (struct sockaddr *) &next_addr,
               sizeof(next_addr));

        if (has_token_initially) {
            struct token token = get_msg_token(client_id, my_port, my_port, NULL);
            sendto(udp_socket, &token, sizeof(token), 0, (struct sockaddr *) &next_addr,
                   sizeof(next_addr));
        }

        while (running) {

            struct token recv_token;

            recvfrom(udp_socket, &recv_token, sizeof(recv_token), 0, NULL, NULL);
            sleep(SLEEP_TIME);
            printf("[%s:%d] [%li]", client_id, my_port, time(NULL));

            if (taken_ports[recv_token.from - MIN_PORT_NUMBER] == 0 &&
                (recv_token.type == INIT_TOKEN || recv_token.type == MSG_TOKEN)) {
                taken_ports[recv_token.from - MIN_PORT_NUMBER] = 1;
                network_size++;
            }

            if (recv_token.type == INIT_TOKEN) {

                printf("\n[INIT_TOKEN] from: %s:%d!\n"
                       "[INIT_TOKEN] Receiver: %d\n", recv_token.sender_id, recv_token.from,
                       recv_token.to);
                if (recv_token.from == my_port) {
                    printf("[INIT_TOKEN] travelled network - REMOVING\n\n");
                } else {
                    if (recv_token.my_next_client_port == next_client_port) {
                        printf("[INIT_TOKEN] new client connects in my place - RESTRUCTURING NETWORK - forwarding to: %d\n\n",
                               recv_token.to);
                        sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                               (struct sockaddr *) &next_addr, sizeof(next_addr));
                        next_client_port = recv_token.from;
                        next_addr.sin_port = htons(next_client_port);
                    } else {
                        printf("[INIT_TOKEN] FORWARDING to port: %d\n\n", next_client_port);
                        sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                               (struct sockaddr *) &next_addr, sizeof(next_addr));
                    }
                }
            } else if (recv_token.type == MSG_TOKEN) {
                if (recv_token.to != my_port) {
                    printf("\n[MSG_TOKEN] msg not to me - FORWARDING to port: %d\n\n",
                           next_client_port);
                    sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                           (struct sockaddr *) &next_addr, sizeof(next_addr));
                } else {
                    printf("\n[MSG_TOKEN] from %s:%d!\n"
                           "[MSG_TOKEN] text: %s\n"
                           "[MSG_TOKEN] SENDING new message to: %d\n\n", recv_token.sender_id,
                           recv_token.from, recv_token.msg, calculate_receiver());

                    char msg[] = "Random message";
                    recv_token = get_msg_token(client_id, my_port, calculate_receiver(), msg);
                    sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                           (struct sockaddr *) &next_addr, sizeof(next_addr));
                }
            } else if (recv_token.type == TERM_TOKEN) {
                taken_ports[recv_token.from - MIN_PORT_NUMBER] = 0;
                network_size--;
                printf("\n[%s:%d] [%li]", client_id, my_port, time(NULL));
                printf("\n[TERM_TOKEN] from: %s:%d!\n"
                       "[TERM_TOKEN] Receiver: %d\n", recv_token.sender_id, recv_token.from,
                       recv_token.to);
                if (recv_token.from == my_port) {
                    printf("[TERM_TOKEN] travelled network - REMOVING\n\n");
                } else {
                    if (recv_token.from == next_client_port) {
                        printf("[TERM_TOKEN] client disconnects in front of me - RESTRUCTURING NETWORK - forwarding to: %d\n\n",
                               recv_token.to);
                        sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                               (struct sockaddr *) &next_addr, sizeof(next_addr));
                        next_client_port = recv_token.my_next_client_port;
                        next_addr.sin_port = htons(next_client_port);
                    } else {
                        printf("[TERM_TOKEN] FORWARDING to port: %d\n\n", next_client_port);
                        sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                               (struct sockaddr *) &next_addr, sizeof(next_addr));
                    }
                }
            }
        }


    }

    return 0;
}

void init_client(int argc, char **argv) {
    if (argc != 6) {
        printf("Wrong number of arguments!\n");
        exit(1);
    }

    client_id = argv[1];
    my_port = (uint16_t) atoi(argv[2]);

    if (my_port < (uint16_t) MIN_PORT_NUMBER ||
        my_port > (uint16_t) (MIN_PORT_NUMBER + MAX_CLIENTS_AMOUNT - 1)) {
        printf("Wrong number of port!\n");
        exit(1);
    }

    char *next_client_address = argv[3];
    char *separator = ":";
    next_client_ip = strtok(next_client_address, separator);
    next_client_port = (uint16_t) atoi(strtok(NULL, ""));

    if (next_client_port < (uint16_t) MIN_PORT_NUMBER ||
        next_client_port > (uint16_t) (MIN_PORT_NUMBER + MAX_CLIENTS_AMOUNT - 1)) {
        printf("Wrong number of port!\n");
        exit(1);
    }


    has_token_initially = atoi(argv[4]);

    if (has_token_initially != 0 && has_token_initially != 1) {
        printf("Wrong value of <has_token_initially>\n");
        exit(1);
    }


    if (strcmp(argv[5], "tcp") == 0) {
        protocol_type = TCP;
    } else if (strcmp(argv[5], "udp") == 0) {
        protocol_type = UDP;
    } else {
        printf("Wrong parameter: protocol_type\n");
        exit(1);
    }

    taken_ports[my_port - MIN_PORT_NUMBER] = 1;
    network_size++;

    printf("\n");
    printf("Client %s initialized succesfully with parameters:\n"
           "Port: %d\n"
           "Next IP: %s\n"
           "Next Port: %d\n"
           "Has token: %d\n"
           "Protocol: %d\n\n", client_id, my_port, next_client_ip, next_client_port,
           has_token_initially, protocol_type);

}


void int_handler(int signum) { // no idea why it works but works ... sometimes

    struct token token = get_term_token(client_id, my_port, next_client_port);
    sendto(udp_socket, &token, sizeof(token), 0, (struct sockaddr *) &next_addr, sizeof(next_addr));

    struct token recv_token;
    recvfrom(udp_socket, &recv_token, sizeof(recv_token), 0, NULL, NULL);


    exit(1);
}

uint16_t calculate_receiver() {

    srand((unsigned int) time(NULL));
    int random = rand() % network_size;
    for (int i = 0; i < MAX_CLIENTS_AMOUNT; i++) {
        if (taken_ports[i] == 1 && random == 0)
            return (uint16_t) (i + MIN_PORT_NUMBER);
        else if (taken_ports[i] == 1 && random > 0)
            random -= 1;
    }
    return my_port;
}

