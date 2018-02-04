FROM clojure
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN lein deps
RUN lein clean
RUN lein cljsbuild once production
EXPOSE 8090
CMD ["lein", "run", "-o"]