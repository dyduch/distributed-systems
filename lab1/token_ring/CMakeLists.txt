cmake_minimum_required(VERSION 3.13)
project(token_ring C)

set(CMAKE_C_STANDARD 11)

add_executable(server udp_server.c)
add_executable(client client.c config.c config.h udp/udp_utils.c udp/udp_utils.h)
