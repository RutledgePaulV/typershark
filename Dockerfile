FROM clojure
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/project.clj
RUN lein deps
RUN lein clean
COPY . /usr/src/app
RUN lein cljsbuild once production
EXPOSE 8090
CMD ["lein", "run", "-o"]