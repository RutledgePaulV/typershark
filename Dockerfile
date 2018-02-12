FROM openjdk:9-slim

ENV LEIN_VERSION=2.8.1
ENV LEIN_INSTALL=/usr/local/bin/

WORKDIR /tmp

RUN apt-get update && apt-get install -y wget gnupg2

# Download the whole repo as an archive
RUN mkdir -p $LEIN_INSTALL \
  && wget -q https://raw.githubusercontent.com/technomancy/leiningen/$LEIN_VERSION/bin/lein-pkg \
  && echo "Comparing lein-pkg checksum ..." \
  && echo "019faa5f91a463bf9742c3634ee32fb3db8c47f0 *lein-pkg" | sha1sum -c - \
  && mv lein-pkg $LEIN_INSTALL/lein \
  && chmod 0755 $LEIN_INSTALL/lein \
  && wget -q https://github.com/technomancy/leiningen/releases/download/$LEIN_VERSION/leiningen-$LEIN_VERSION-standalone.zip \
  && wget -q https://github.com/technomancy/leiningen/releases/download/$LEIN_VERSION/leiningen-$LEIN_VERSION-standalone.zip.asc \
  && gpg --keyserver pool.sks-keyservers.net --recv-key 2B72BF956E23DE5E830D50F6002AF007D1A7CC18 \
  && echo "Verifying Jar file signature ..." \
  && gpg --verify leiningen-$LEIN_VERSION-standalone.zip.asc \
  && rm leiningen-$LEIN_VERSION-standalone.zip.asc \
  && mkdir -p /usr/share/java \
  && mv leiningen-$LEIN_VERSION-standalone.zip /usr/share/java/leiningen-$LEIN_VERSION-standalone.jar

ENV PATH=$PATH:$LEIN_INSTALL
ENV LEIN_ROOT 1

# Install clojure 1.9.0 so users don't have to download it every time
RUN echo '(defproject dummy "" :dependencies [[org.clojure/clojure "1.9.0"]])' > project.clj \
  && lein deps && rm project.clj

WORKDIR /usr/src/app

COPY project.clj /usr/src/app/project.clj
RUN lein deps
RUN lein clean
COPY . /usr/src/app
RUN lein cljsbuild once production
RUN lein minify-assets

RUN rm resources/public/css/login.css && \
    mv resources/public/css/login.min.css resources/public/css/login.css

RUN rm resources/public/css/styles.css && \
    mv resources/public/css/styles.min.css resources/public/css/styles.css

RUN rm resources/public/js/main.js && \
    mv resources/public/js/main.min.js resources/public/js/main.js

EXPOSE 8090
CMD ["lein", "run", "-o"]