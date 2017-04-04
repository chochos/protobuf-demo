package main

import (
	"fmt"
	proto "github.com/golang/protobuf/proto"
	"log"
	"net"
	"protos_wrapper"
	"time"
)

func read(conn net.Conn, buf []byte) (*protos_wrapper.Respuesta, error) {
	conn.Read(buf[0:2])
	largo := (buf[0] << 8) | buf[1]
	conn.Read(buf[0:largo])
	resp := new(protos_wrapper.Respuesta)
	err := proto.Unmarshal(buf[0:largo], resp)
	return resp, err
}

func write(conn net.Conn, req *protos_wrapper.Peticion, hlen []byte) error {
	data, err := proto.Marshal(req)
	hlen[0] = byte((len(data) & 0xff00) >> 8)
	hlen[1] = byte(len(data) & 0xff)
	conn.Write(hlen)
	conn.Write(data)
	return err
}

func main() {
	conn, err := net.Dial("tcp", "localhost:9999")
	if err != nil {
		log.Fatal("No puedo conectarme al servidor de Protobuf")
		return
	}
	req := new(protos_wrapper.Peticion)
	req.User = "usuario"
	req.Password = "Password"
	req.Product = "Producto"
	req.Amount = 12.34
	req.Account = "CuentaDestino"
	req.Date = time.Now().UnixNano() / 1000000

	done := make(chan struct{})
	t0 := time.Now().UnixNano()
	go func() {
		var buf = make([]byte, 4096)
		for i := 0; i < 50000; i++ {
			resp, err := read(conn, buf)
			if err != nil {
				log.Fatal("Leyendo respuestas del server")
				return
			}
			fmt.Println(resp)
		}
		fmt.Println("READ done")
		done <- struct{}{}
	}()
	var hlen = make([]byte, 2)
	for i := 0; i < 50000; i++ {
		req.Id = int32(i)
		req.Date = time.Now().UnixNano() / 1000000
		write(conn, req, hlen)
	}
	//Wait until all responses are done
	fmt.Println("WRITE done")
	<-done
	conn.Close()
	t1 := time.Now().UnixNano()
	fmt.Println("TIME: ", (t1-t0)/1000000, " milis")
}
