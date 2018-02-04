(ns typershark.middleware
  (:require [cemerick.friend.workflows :as workflows]
            [cemerick.friend.util :refer :all]
            [cemerick.friend :as friend]
            [digest :as digest]
            [ring.util.request :as req]))

(defn gravatar [email]
  (let [md5 (digest/digest "md5" email)]
    (str "https://en.gravatar.com/avatar/" md5 "?d=mm")))

(defn- username
  [form-params params]
  (or (get form-params "username") (:username params "")))

(defn interactive-form
  [& {:keys [redirect-on-auth?] :as form-config :or {redirect-on-auth? true}}]
  (fn [{:keys [request-method params form-params] :as request}]
    (when (and (= (gets :login-uri form-config (::friend/auth-config request))
                  (req/path-info request)) (= :post request-method))
      (let [creds {:username (username form-params params)}
            {:keys [username]} creds]
        (if-let [user-record (and username
                                  ((gets :credential-fn form-config (::friend/auth-config request))
                                    (with-meta creds {::friend/workflow :interactive-form})))]
          (workflows/make-auth user-record
            {::friend/workflow :interactive-form ::friend/redirect-on-auth? redirect-on-auth?})
          ((or (gets :login-failure-handler form-config (::friend/auth-config request))
               #'workflows/interactive-login-redirect)
            (update-in request [::friend/auth-config] merge form-config)))))))

(defn credential-fn [{:keys [username]}]
  {:email    username
   :gravatar (gravatar username)})

(defn wrap-authentication [handler]
  (friend/authenticate
    handler
    {:credential-fn credential-fn
     :allow-anon?   false
     :workflows     [(interactive-form)]}))