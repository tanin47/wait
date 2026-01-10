Wait
=====

[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/io.github.tanin47/wait.svg)](https://central.sonatype.com/artifact/io.github.tanin47/wait)
![Github Actions](https://github.com/tanin47/wait/actions/workflows/ci.yml/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/tanin47/wait/graph/badge.svg?token=BGQU70MAUP)](https://codecov.io/gh/tanin47/wait)

Wait is an self-hostable CORS-enabled headless wait list system that connects to Google Sheets. No database is needed.

Wait supports CORS. Your static page can invoke `fetch(..)` to the Wait server from *a different domain*. 
This means your static page doesn't need a backend and can be hosted for free anywhere e.g. Netlify, Github Pages.

Wait supports multiple groups. You can host one Wait server that serves multiple websites or landing pages.
It's the cheapest option for a waiting list system. You can get a ~$4/month VPS to host Wait and power >10 websites and landing pages at the same time.


How to integrate with a Wait server
------------------------------------

Let's assume you host a Wait server at: `waitserver.com`

On your `yournewproduct.com`, you can have the following code that performs a cross-domain `fetch(..)` to the Wait server:

```
async function joinWaitlist(email) {
    const resp = await fetch('http://waitserver.com/write', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
          email,
          group: 'your-group-name' // for the scenario where you have multiple websites.
      })
    })

    if (resp.status === 200) {
      // Done
    } else if (resp.status === 400) {
      const json = await resp.json()
      console.log(json.error)
    } else {
      console.log(resp)
    }
}

// Somewhere else, there is a button that triggers `joinWaitlist(email)` when clicked.
```

Next, we want to set up a Google Sheet and run a Wait server!


How to set up a Google Sheet and a Google service account
----------------------------------------------------------

You will need to set up a service account and a Google Sheet.

1. Go to https://console.cloud.google.com/apis/library/sheets.googleapis.com and enable Google Sheets API.
2. Go to https://console.cloud.google.com/apis/credentials and add a service account.
3. Go into the service account. Go to the Keys tab. Create a JSON key.
4. Wait for the JSON key file to finish being downloaded.
5. Store the *content* of the JSON key file in the environment variable: `GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON`. We'll later use it.

We've set up a service account successfully. 

Next, we'll set up a Google Sheet.

1. Go to https://docs.google.com/spreadsheets/u/0/ and create a new sheet.
2. Share the sheet with the service account's email that you previously created. Ensure the service account is an *editor*.
3. Extract the Google Sheet ID from the url: `https://docs.google.com/spreadsheets/d/<GOOGLE_SHEET_ID>/edit?gid=0#gid=0`. Store the sheet ID in the env variable: `GOOGLE_SHEET_ID`.
4. Note the sheet name that you want to use in the Google Sheet doc. The default is `Sheet1`. Store the sheet name in the env variable: `GOOGLE_SHEET_NAME`.

The output of this step is 3 environment variables: 

- `GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON`
- `GOOGLE_SHEET_ID`
- `GOOGLE_SHEET_NAME`

How to run a Wait server
----------------------------

There are 2 ways to run Wait:

1. Run as a standalone: Docker, Render.com, and JAR
2. Embed your website into a larger system

### 1. Run as a standalone

__<ins>Use Docker</ins>__

The docker image is here: https://hub.docker.com/repository/docker/tanin47/wait

```
export GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON=<your_json_key_in_string>
export GOOGLE_SERVICE_ACCOUNT_SHEET_ID=<your_google_sheet_id>
export GOOGLE_SERVICE_ACCOUNT_SHEET_PRIVATE_KEY=<your_google_sheet_name>
docker run -p 9090:9090 \
           --entrypoint "" \
           --pull always \
           tanin47/wait:1.0.0 \
           java -jar wait-1.0.0.jar
```

__<ins>Use Render.com</ins>__

Set the following environment variables:
- GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON
- GOOGLE_SERVICE_ACCOUNT_SHEET_ID
- GOOGLE_SERVICE_ACCOUNT_SHEET_PRIVATE_KEY

The file [render.yaml](./render.yaml) shows a blueprint example of how to run Wait on Render.

__<ins>Run from the JAR file</ins>__

First, you can download the `wait-VERSION.jar` file from
the [Releases](https://github.com/tanin47/embeddable-java-web-framework/releases) page.

Then, you can run the command below:

```
export GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON=<your_service_account_key_json_in_string>
export GOOGLE_SERVICE_ACCOUNT_SHEET_ID=<your_google_sheet_id>
export GOOGLE_SERVICE_ACCOUNT_SHEET_PRIVATE_KEY=<your_google_sheet_name>
java -jar wait-1.0.0.jar
```

Then, you can visit http://localhost:9090


### 2. Embed your website into a larger system

If you are using JVM, you can embed Wait into your already running system. No need for a separate deployment.

1. The larger system should include your fat JAR as a dependency by adding the below dependency:

```
<dependency>
    <groupId>io.github.tanin47</groupId>
    <artifactId>wait</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Instantiate the website with the port 9090 when the larger system initializes:

```java
var main = new tanin.wait.WaitServer(
  9090, 
  googleServiceAccountKeyJsonInString, 
  googleSheetId,
  googleSheetName
);
main.start();
```

3. Visit http://localhost:9090 to confirm that the server is working.


-------------------

How to develop
---------------

1. Run `npm install` to install all dependencies.
2. Run `./gradlew run` to run the web server.
3. On a separate terminal, run `npm run hmr` in order to hot-reload the frontend code changes.


Publish JAR
------------

This flow has been set up as the Github Actions workflow: `publish-jar`.

EJWF is a template repository with collections of libraries and conventions. It's important that you understand
each build process and are able to customize to your needs.

Here's how you can build your fat JAR:

1. Set up `~/.jreleaser/config.toml` with `JRELEASER_MAVENCENTRAL_USERNAME` and `JRELEASER_MAVENCENTRAL_PASSWORD`
2. Run `./gradlew clean publish jreleaserDeploy`. This step is IMPORTANT to clean out the previous versions.


Publish Docker
---------------

This flow has been set up as a part of the Github Actions workflow: `create-release-and-docker`.

1. Run `docker buildx build --platform linux/amd64,linux/arm64 -t wait:<LATEST_VERSION> .`
2. Test locally with:
   `docker run -p 9090:9090 --entrypoint "" wait:1.0.0 java -jar wait-1.0.0.jar -port 9090`
3. Run: `docker tag wait:1.0.0 tanin47/wait:1.0.0`
4. Run: `docker push tanin47/wait:1.0.0`
5. Go to Render.com, sync the blueprint, and test that it works

Release a new version
----------------------

1. Create an empty release with a new tag. The tag must follow the format: `vX.Y.Z`.
2. Go to Actions and wait for the `create-release-and-docker` (which is triggered automatically) workflow to finish.
3. Test the docker with
   `docker run -p 9090:9090 --entrypoint "" tanin47/wait:1.0.0 java -jar wait-1.0.0.jar -port 9090`.
4. Go to Actions and trigger the workflow `publish-jar` on the tag `vX.Y.Z` in order to publish the JAR to Central
   Sonatype.
