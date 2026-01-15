// Package bbbapi provides the functionality for quickly
// standing up a new API server.
package bbbapi

import (
	"log/slog"

	"github.com/bigbluebutton/bigbluebutton/bbb-api/internal/core/bbbhttp"
)

// A DefaultAPI is composed of a server that can be run at
// the specified address.
type DefaultAPI struct {
	*bbbhttp.Server
	address string
}

// Start begins running the API at a specific address.
func (api *DefaultAPI) Start() {
	slog.Info("API started at " + api.address)
	api.ListenAndServe(api.address)
}

// NewDefaultAPI creates a new API that is accessible at the given address.
// Register is used by the underlying server to define the routes that
// the API can handle requests on.
func NewDefaultAPI(address string, register func(server *bbbhttp.Server)) *DefaultAPI {
	api := configureAPI(address)
	register(api.Server)
	return api
}

func configureAPI(address string) *DefaultAPI {
	bbbServer := bbbhttp.NewDefaultServer()
	return &DefaultAPI{
		Server:  bbbServer,
		address: address,
	}
}
