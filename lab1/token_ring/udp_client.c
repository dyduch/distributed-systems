#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <ctype.h>
#include "config.h"

int main() {

    int client_socket;
    int port_number;
    int n_bytes;

    char buffer[1024];
    struct sockaddr_in server_address;
    socklen_t addr_size;

    // Create UDP Socket
    client_socket = socket(PF_INET, SOCK_DGRAM, 0);

    // Configure settings in address struct
    server_address.sin_family = AF_INET;
    int port = get_port();
    server_address.sin_port = htons((uint16_t) port);
    server_address.sin_addr.s_addr = inet_addr("127.0.0.1");
    memset(server_address.sin_zero, '\0', sizeof server_address.sin_zero);

    // Initialize size variable to be used later on
    addr_size = sizeof server_address;

    int received = 0;

    while(1) {

        char action[1];
        printf("Type action: \n");
        fgets(action, 1, stdin);

        if(action[0] == 's') {

            char rec_port[4];
            printf("Type port: \n");
            fgets(rec_port, 4, stdin);

            int port ;
            sscanf(rec_port, "%d", &port);

            server_address.sin_port = htons((uint16_t) port);


            printf("Type sentence to send to server: \n");
            fgets(buffer, 1024, stdin);
            printf("you typed: %s\n", buffer);

            n_bytes = strlen(buffer) + 1;

            // Send message to server
            sendto(client_socket, buffer, n_bytes, 0, (struct sockaddr *) &server_address, addr_size);

        }
        else
            printf("wrong type of msg \n");

    }
    return 0;
}