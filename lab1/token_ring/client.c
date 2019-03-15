#include <unistd.h>
#include <time.h>
#include "config.h"
#include "udp/udp_utils.h"
#include "tcp/tcp_utils.h"

void init_client(int argc, char **argv);
void init_logger();

// client parameters
char *client_id;
uint16_t my_port;
char *next_client_ip;
uint16_t next_client_port;
int has_token_initially;
int protocol_type;

struct sockaddr_in my_addr;
struct sockaddr_in next_addr;
struct sockaddr_in logger_addr;
int logger_socket;



int udp_socket;
int tcp_snd_socket;
int tcp_rcv_socket;
int tcp_socket;

int running = 1;

int taken_ports[MAX_CLIENTS_AMOUNT] = {0};
int network_size = 0;

char* logger_ip = "237.7.0.1";
uint16_t logger_port = 8999;


void int_handler(int);
char* randstring(size_t);

int main(int argc, char **argv) {

    init_client(argc, argv);
    init_logger();

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

                send_to_logger(client_id, &logger_addr, logger_socket);

                if (recv_token.to != my_port) {
                    printf("\n[MSG_TOKEN] msg not to me - FORWARDING to port: %d\n\n",
                           next_client_port);
                    sendto(udp_socket, &recv_token, sizeof(recv_token), 0,
                           (struct sockaddr *) &next_addr, sizeof(next_addr));
                } else {
                    char* msg = randstring(10);
                        printf("\n[MSG_TOKEN] from %s:%d!\n"
                           "[MSG_TOKEN] text: %s\n"
                           "[MSG_TOKEN] SENDING new message {%s} to: %d\n\n", recv_token.sender_id,
                           recv_token.from, recv_token.msg, msg, calculate_receiver());

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

    else if (protocol_type == TCP) {

        tcp_rcv_socket = socket(AF_INET, SOCK_STREAM, 0);
        tcp_init_my_socket(&my_addr, my_port);
        bind(tcp_rcv_socket, (const struct sockaddr *) &my_addr, sizeof(my_addr));
        listen(tcp_rcv_socket, 10);

        tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);

        struct token init_token = get_init_token(client_id, my_port, next_client_port);
        tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);

        next_addr.sin_family = AF_INET;
        next_addr.sin_addr.s_addr = htonl(INADDR_ANY);
        next_addr.sin_port = htons(next_client_port);
        connect(tcp_snd_socket, (const struct sockaddr *) &next_addr, sizeof(next_addr));

        write(tcp_snd_socket, &init_token, sizeof(init_token));
        close(tcp_snd_socket);

        if(has_token_initially){
            struct token token = get_msg_token(client_id, my_port, my_port, NULL);

            tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);

            next_addr.sin_family = AF_INET;
            next_addr.sin_addr.s_addr = htonl(INADDR_ANY);
            next_addr.sin_port = htons(next_client_port);
            connect(tcp_snd_socket, (const struct sockaddr *) &next_addr,
                    sizeof(next_addr));

            write(tcp_snd_socket, &token, sizeof(token));
            close(tcp_snd_socket);

        }

        while (running) {

            struct token recv_token;

            tcp_socket = accept(tcp_rcv_socket, NULL, NULL);
            read(tcp_socket, &recv_token, sizeof(recv_token));

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

                        tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);
                        tcp_setup_successor(&next_addr, next_client_ip, next_client_port);
                        connect(tcp_snd_socket, (const struct sockaddr *) &next_addr,
                                sizeof(next_addr));
                        write(tcp_snd_socket, &recv_token, sizeof(recv_token));
                        close(tcp_snd_socket);

                        next_client_port = recv_token.from;
                        next_addr.sin_port = htons(next_client_port);
                    } else {
                        printf("[INIT_TOKEN] FORWARDING to port: %d\n\n", next_client_port);
                        tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);
                        tcp_setup_successor(&next_addr, next_client_ip, next_client_port);
                        connect(tcp_snd_socket, (const struct sockaddr *) &next_addr,
                                sizeof(next_addr));
                        write(tcp_snd_socket, &recv_token, sizeof(recv_token));
                        close(tcp_snd_socket);

                    }
                }
            } else if (recv_token.type == MSG_TOKEN) {

                send_to_logger(client_id, &logger_addr, logger_socket);

                if (recv_token.to != my_port) {
                    printf("\n[MSG_TOKEN] msg not to me - FORWARDING to port: %d\n\n",
                           next_client_port);

                    tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);
                    tcp_setup_successor(&next_addr, next_client_ip, next_client_port);
                    connect(tcp_snd_socket, (const struct sockaddr *) &next_addr,
                            sizeof(next_addr));

                    write(tcp_snd_socket, &recv_token, sizeof(recv_token));
                    close(tcp_snd_socket);


                } else {
                    char *msg = randstring(10);
                    printf("\n[MSG_TOKEN] from %s:%d!\n"
                           "[MSG_TOKEN] text: %s\n"
                           "[MSG_TOKEN] SENDING new message {%s} to: %d\n\n", recv_token.sender_id,
                           recv_token.from, recv_token.msg, msg, calculate_receiver());

                    recv_token = get_msg_token(client_id, my_port, calculate_receiver(), msg);
                    tcp_snd_socket = socket(AF_INET, SOCK_STREAM, 0);
                    tcp_setup_successor(&next_addr, next_client_ip, next_client_port);
                    connect(tcp_snd_socket, (const struct sockaddr *) &next_addr,
                            sizeof(next_addr));

                    write(tcp_snd_socket, &recv_token, sizeof(recv_token));
                    close(tcp_snd_socket);
                }
            }


        }
    }

    return 0;
}

void init_logger(){
    logger_socket = socket(AF_INET, SOCK_DGRAM, 0);

    logger_addr.sin_family = AF_INET;
    logger_addr.sin_addr.s_addr = inet_addr(logger_ip);
    logger_addr.sin_port = htons(logger_port);
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

    if (protocol_type == UDP) {
        struct token token = get_term_token(client_id, my_port, next_client_port);
        sendto(udp_socket, &token, sizeof(token), 0, (struct sockaddr *) &next_addr,
               sizeof(next_addr));

        struct token recv_token;
        recvfrom(udp_socket, &recv_token, sizeof(recv_token), 0, NULL, NULL);
    }

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

char* randstring(size_t length) {

    static char charset[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789,.-#'?!";
    char *random_string = NULL;

    if (length) {
        random_string = malloc(sizeof(char) * (length +1));

        if (random_string) {
            for (int n = 0;n < length;n++) {
                int key = rand() % (int)(sizeof(charset) -1);
                random_string[n] = charset[key];
            }
            random_string[length] = '\0';
        }
    }
    return random_string;
}

