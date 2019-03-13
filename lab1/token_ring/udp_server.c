#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <ctype.h>

int main() {

    int udp_socket;
    int n_bytes;
    char buffer[1024];
    struct sockaddr_in server_address;
    struct sockaddr_in client_address;
    struct sockaddr_storage server_storage;
    socklen_t address_size;
    socklen_t client_address_size;
    int i;

    /* Create UDP Socket */

    udp_socket = socket(PF_INET, SOCK_DGRAM, 0);

    /* Configure settings in address structure */

    server_address.sin_family = AF_INET;
    server_address.sin_port = htons(9008);
    server_address.sin_addr.s_addr = inet_addr("127.0.0.1");
    memset(server_address.sin_zero, '\0', sizeof(server_address.sin_zero));

    /* Bind socket with address structure */

    bind(udp_socket, (struct sockaddr *) &server_address, sizeof(server_address));

    address_size = sizeof server_storage;

    while(1) {
        /* Try to receive UDP datagram. Address and port of
         * requesting client will be stored in server_storage variable */

        n_bytes = recvfrom(udp_socket, buffer, 1024, 0, (struct sockaddr *) &client_address, &address_size);

        /* Convert received message to uppercase */
        for( i = 0; i < n_bytes; i++) {
            buffer[i] = toupper(buffer[i]);
        }

        /* Send uppercase message back to clkient using sever_storage as the address */
        sendto(udp_socket, buffer, n_bytes, 0, (struct sockaddr *) &client_address, address_size);
    }
    return 0;
}