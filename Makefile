.PHONY: build-docker

build-docker:
	sbt assembly
	docker build -t mychat:0.1.0 -f Dockerfile .
