function countTimeline(message: string, object?: Object) {

}

function enterTimeline(message: string) {

}

function leaveTimeline(message?: string) {

}

module J2ME {
  export class CompilerBailout {
    constructor(public message: string) {
      // ...
    }
    toString(): string {
      return "CompilerBailout: " + this.message;
    }
  }
}